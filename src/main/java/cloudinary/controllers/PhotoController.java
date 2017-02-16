package cloudinary.controllers;

import cloudinary.bean.CloudinaryImpl;
import cloudinary.lib.PhotoUploadValidator;
import cloudinary.models.Photo;
import cloudinary.models.PhotoUpload;
import cloudinary.repositories.ImageObject;
import cloudinary.repositories.PhotoRepository;
import cloudinary.util.CSVUtils;
import cloudinary.util.Directory;
import cloudinary.util.DownloadFile;
import cloudinary.util.ImageResize;
import cloudinary.models.*;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.xmp.XmpDirectory;
import cloudinary.util.SendAttachmentInEmail;
import com.cloudinary.Singleton;
import com.cloudinary.SingletonManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/")
public class PhotoController {
    @Autowired
    private PhotoRepository photoRepository;
    
    @Autowired
    private MessageSource messageSource;
    
    public Cloudinary cloudinary;
    
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String listPhotos(ModelMap model) throws ImageProcessingException, IOException {
        model.addAttribute("photos", photoRepository.findAll());
        
        if (cloudinary == null) {
        	cloudinary = new Cloudinary(ObjectUtils.asMap(
  				  "cloud_name", messageSource.getMessage("system.cloud.name",null,null),
  				  "api_key", messageSource.getMessage("system.api.key",null,null),
  				  "api_secret", messageSource.getMessage("system.api.secret",null,null)));
          
          SingletonManager manager = new SingletonManager();
	  		manager.setCloudinary(cloudinary);
	  		manager.init();
        }
        
        return "photos";
    }

    @SuppressWarnings("rawtypes")
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
    public void uploadPhoto(@ModelAttribute PhotoUpload photoUpload, BindingResult result, ModelMap model,HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, ImageProcessingException, URISyntaxException {
        PhotoUploadValidator validator = new PhotoUploadValidator();
        //validator.validate(photoUpload, result);
        
        //HashMap<String,ArrayList<String>> fileLst = new HashMap<String,ArrayList<String>>();
        HashMap<String,HashMap<String,ArrayList<String>>> fileLst = new HashMap<String,HashMap<String,ArrayList<String>>>();
        
        HashMap<String,ArrayList<String>> fileArray = new HashMap<String,ArrayList<String>>();
        ArrayList<String> skuFinalFolder = new ArrayList<String>();
        
        ArrayList<String> skuTransFolder = new ArrayList<String>();
        
        ArrayList<String> failList = new ArrayList<String>();
        
        Map uploadResult = null;
        
        boolean failExist = false;
        
        ImageObject imgObj;
        ArrayList<ImageObject> imgObjLst = new ArrayList<ImageObject>();
        ImageObject failObj;
        List<ImageObject> dtoFail = new ArrayList<ImageObject>();
        
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HHmmss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        
        String rootPath = System.getProperty("catalina.home");
        File dir2 = new File(rootPath + File.separator + "tmpFiles" + File.separator + "ProcessedImage");
		
		if (!dir2.exists())
			dir2.mkdirs();
		
		File dir3 = new File(rootPath + File.separator + "tmpFiles" + File.separator + "ProcessedImage" + File.separator + strDate);
		
		if (!dir3.exists())
			dir3.mkdirs();
		
		BufferedWriter writer2 = null;
		
		try {
			String backupFile = dir3 + File.separator + "ListBackup.csv";
			
			String content = "";
			
			writer2 = new BufferedWriter( new FileWriter( backupFile,true));
		    writer2.write( "sku,Transparent Background Image,Image2,Image3,Image4");
		    writer2.newLine();
			
	        if (photoUpload.getFolderPath() != null && !photoUpload.getFolderPath().isEmpty()) {
	        	Directory direct = new Directory();
	        	try {
	        		fileLst = direct.listf(photoUpload.getFolderPath());
	        	} catch (IllegalArgumentException iae) {
	        		model.addAttribute("message", "There is un-process folder " + iae.getMessage());
	                return;
	        	}
	        	
	        	String tmpStr;
	        	boolean transImageCheck = false;
	        	//int anchor = 0;
	        	
	        	for (String keyFolderName : fileLst.keySet()) {
	        		
	        		//anchor = 0;
	        		
	        		transImageCheck = false;
	        		
	        		fileArray = fileLst.get(keyFolderName);
	        		
	        		imgObj = new ImageObject();
	        		
	        		content = keyFolderName + ",";
	        		
	        		imgObj.setSku(keyFolderName);
	        		
	        		skuFinalFolder = fileArray.get("FINAL");
	        		skuTransFolder = fileArray.get("TRANSPARENT");
	        		
	        		if (fileArray.get("FAIL") != null) {
	        			failList = fileArray.get("FAIL");
	        			if (failList != null && failList.size() > 0) {
	        				failExist = true;
	        				continue;
	        			}
	        		} 
	        		
	        		
	        		
	        		for (int i=0; i< skuTransFolder.size(); i++) {
	        			File pic = new File(skuTransFolder.get(i));
	        			tmpStr = "";
	        			
	        			int pos = pic.getName().lastIndexOf(".");
	        			if (pos > 0) {
	        				tmpStr = pic.getName().substring(0, pos);
	        			} else {
	        				tmpStr = pic.getName();
	        			}
	        			
	        			uploadResult = cloudinary.uploader().upload(pic,
	                            ObjectUtils.asMap("public_id",tmpStr,
	                            					"folder",keyFolderName,
	                            					"resource_type", "auto"));
	        			
	        			
	        			content = content + (String) uploadResult.get("url") + ",";
	        			
	    				imgObj.setNewImage1((String) uploadResult.get("url"));
	        		}
	        		
	        		for (int i=0; i< skuFinalFolder.size(); i++) {
	        			File pic = new File(skuFinalFolder.get(i));
	        			tmpStr = "";
	        			
	        			int pos = pic.getName().lastIndexOf(".");
	        			if (pos > 0) {
	        				tmpStr = pic.getName().substring(0, pos);
	        			} else {
	        				tmpStr = pic.getName();
	        			}
	        			
	        			uploadResult = Singleton.getCloudinary().uploader().upload(pic,
	                            ObjectUtils.asMap("public_id",tmpStr,
	                            					"folder",keyFolderName,
	                            					"resource_type", "auto"));
	        			
	    				if (imgObj.getNewImage2() == null) {
	    					content = content + (String) uploadResult.get("url") + ",";
	    					imgObj.setNewImage2((String) uploadResult.get("url"));
	    				}
	    				else if (imgObj.getNewImage3() == null) {
	    					content = content + (String) uploadResult.get("url") + ",";
	    					imgObj.setNewImage3((String) uploadResult.get("url"));
	    				}
	    				else if (imgObj.getNewImage4() == null) {
	    					content = content + (String) uploadResult.get("url");
	    					imgObj.setNewImage4((String) uploadResult.get("url"));
	    				}
	        		}
	        		
	        		writer2.write(content);
	        		writer2.newLine();
	        		
	        		//Move to backup folder
	        		String copyTo = "";
	        		
	        		copyTo = dir2.getAbsolutePath() + File.separator + strDate + File.separator  + keyFolderName;
	        		
	        		FileUtils.copyDirectory(new File(photoUpload.getFolderPath() + File.separator + keyFolderName), new File(copyTo));
	        		
	        		try {
	        			FileUtils.deleteDirectory(new File(photoUpload.getFolderPath() + File.separator + keyFolderName));
	        		} catch (Exception e) {
	        			continue;
	        		}
	        		imgObjLst.add(imgObj);
	        	}
	        	
	        }
		} catch ( Exception e)
		{
			System.out.println("IOException occurred." + e.getMessage());
		} finally
		{
			try
			{
				if ( writer2 != null)
					writer2.close( );
			}
			catch ( Exception e)
			{
				System.out.println("IOException occurred." + e.getMessage());
			}
		}
		
        
		File dir = new File(rootPath + File.separator + "tmpFiles");
		if (!dir.exists())
			dir.mkdirs();
        
      //Write to CSV file
		String fileName = "result";
		if (photoUpload.getFileName() != null && !photoUpload.getFileName().isEmpty()) {
			fileName = photoUpload.getFileName();
		}
		
		final String csvFile = dir.getAbsolutePath() + File.separator + fileName + ".csv";
        FileWriter writer = new FileWriter(csvFile);
        
        CSVUtils.writeLine(writer, Arrays.asList("sku",
        		"Transparent Background Image",
        		"Image2",
        		"Image3",
        		"Image4"));
        
        if (imgObjLst != null) {
	        for (int i=0; i < imgObjLst.size(); i++) {
	        	CSVUtils.writeLine(writer, Arrays.asList(imgObjLst.get(i).getSku(),
	        			imgObjLst.get(i).getNewImage1(),
	        			imgObjLst.get(i).getNewImage2(),
	        			imgObjLst.get(i).getNewImage3(),
	        			imgObjLst.get(i).getNewImage4()));
	        }
	        
	        writer.flush();
	        writer.close();
	        
	        //Download
	        File fileDown = new File(csvFile);
			if(!fileDown.exists()){
				throw new ServletException("File doesn't exists on server.");
			}
			
			
			
			InputStream fis = new FileInputStream(fileDown);
			response.setContentType("application/octet-stream");
			response.setContentLength((int) fileDown.length());
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".csv" + "\"");

			ServletOutputStream os = response.getOutputStream();
			byte[] bufferData = new byte[1024];
			int read=0;
			while((read = fis.read(bufferData))!= -1){
				os.write(bufferData, 0, read);
			}
			os.flush();
			os.close();
			fis.close();
	        
	        String mailBody = "Upload " + imgObjLst.size() + " products";
	        sendMail(strDate, mailBody, new ArrayList<String>() {{add(csvFile);}});
	        
	        Files.deleteIfExists(fileDown.toPath()); 
        }
		
		dir = new File(rootPath + File.separator + "tmpFiles" + File.separator + "FailUploadImage");
		if (!dir.exists())
			dir.mkdirs();
		
		if (failExist) {
			
			final String csvFileFail = dir.getAbsolutePath() + File.separator + "Fail_" + strDate + ".csv";
	        writer = new FileWriter(csvFileFail);
	        
	        CSVUtils.writeLine(writer, Arrays.asList("SKU ","Reason","Input Line"));
	                
	        for (String keyFolderName : fileLst.keySet()) {
	        	fileArray = fileLst.get(keyFolderName);
				if (fileArray.get("FAIL") != null) {
	    			failList = fileArray.get("FAIL");
	    			if (failList != null && failList.size() > 0) {
	    				CSVUtils.writeLine(writer, Arrays.asList(keyFolderName,failList.get(0),""));
	    			}
	    		} 
			}
	        
	        writer.flush();
	        writer.close();
		}
        
		
		
        if (result.hasErrors()){
            model.addAttribute("photoUpload", photoUpload);
            return;
        } else {
            Photo photo = new Photo();
            photo.setTitle(photoUpload.getTitle());
            photo.setUpload(photoUpload);
            model.addAttribute("upload", uploadResult);
            //photoRepository.save(photo);
            model.addAttribute("photo", photo);
            return;
        }
    }
    
    @SuppressWarnings("rawtypes")
	@RequestMapping(value = "/downloadImage", method = RequestMethod.POST)
    public String downloadImage(@ModelAttribute PhotoUpload photoUpload, BindingResult result, ModelMap model) throws IOException {
        PhotoUploadValidator validator = new PhotoUploadValidator();
        //validator.validate(photoUpload, result);
        
        File dir;
        ImageObject failObj;
        List<ImageObject> dto;
        List<ImageObject> dtoFail = new ArrayList<ImageObject>();
        Map<String, List<ImageObject>> resultXls = new HashMap<String, List<ImageObject>>();
        String rootPath = System.getProperty("catalina.home");
        
        File dir2 = new File(rootPath + File.separator + "tmpFiles" + File.separator + "Image");
		
		if (!dir2.exists())
			dir2.mkdirs();
        
        try {
			byte[] bytes = photoUpload.getFile().getBytes();

			// Creating the directory to store file
			
			dir = new File(rootPath + File.separator + "tmpFiles");
			if (!dir.exists())
				dir.mkdirs();
			
			String filePath = "";
			
			filePath = dir.getAbsolutePath() + File.separator + photoUpload.getFile().getOriginalFilename();

			// Create the file on server
			File serverFile = new File(filePath);
			BufferedOutputStream stream = new BufferedOutputStream(
					new FileOutputStream(serverFile));
			stream.write(bytes);
			stream.close();

			String imageSubPath;
			
			dto = new ArrayList<ImageObject>();
			
			ImageXlsProcess dataGrabP2 = new ImageXlsProcess();
			
			resultXls = dataGrabP2.doPlaceOrder(filePath);
			
			dto = resultXls.get("SUCCESS");
			dtoFail = resultXls.get("FAIL");
			
			ArrayList<String> sample;
			DownloadFile downloadF = new DownloadFile();
			
			for (int j=0; j<dto.size(); j++) {
				try {
					imageSubPath = dir2.getAbsolutePath() + File.separator + dto.get(j).getSku();
			        sample = new ArrayList<String>();
			        sample.add(dto.get(j).getImage1());
			        sample.add(dto.get(j).getImage2());
			        sample.add(dto.get(j).getImage3());
			        sample.add(dto.get(j).getImage4());
			        
			        downloadF.downloadFile(sample, imageSubPath);
		        
				} catch (Exception ioe) {
					failObj = new ImageObject();
		        	
		        	failObj.setSku(dto.get(j).getSku());
		            
		        	failObj.setReason("Download failed. " + ioe.getMessage());
		        	failObj.setLine("");
		        	dtoFail.add(failObj);
		        	
		        	continue;
				}
			}
			
        } catch (Exception e) {
        	model.addAttribute("message", "Error occured");
            return "download_form";
        }
        
        //Resize all
        
        ImageResize.doResize(dir2.getAbsolutePath());
        
        dir = new File(rootPath + File.separator + "tmpFiles" + File.separator + "FailDownloadImage");
		if (!dir.exists())
			dir.mkdirs();
        
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HHmmss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        FileWriter writer;
        
        if (dtoFail != null && dtoFail.size() > 0) {
        	
        	final String csvFileFail = dir.getAbsolutePath() + File.separator + "Fail_" + strDate + ".csv";
	        writer = new FileWriter(csvFileFail);
	        
	        CSVUtils.writeLine(writer, Arrays.asList("SKU ","Reason","Input Line"));
	        
	        for (int i=0; i < dtoFail.size(); i++) {
	        	CSVUtils.writeLine(writer, Arrays.asList(dtoFail.get(i).getSku(),dtoFail.get(i).getReason(), dtoFail.get(i).getLine()), ',', '"');
	        }
	        
	        writer.flush();
	        writer.close();
        }    
	    
        model.addAttribute("message", "Image downloaded : " + dir2.getAbsolutePath());
        
        return "download_form";
    }

    @RequestMapping(value = "/upload_form", method = RequestMethod.GET)
    public String uploadPhotoForm(ModelMap model) {
        model.addAttribute("photoUpload", new PhotoUpload());
        return "upload_form";
    }
    
    @RequestMapping(value = "/download_form", method = RequestMethod.GET)
    public String downloadPhotoForm(ModelMap model) {
        model.addAttribute("photoUpload", new PhotoUpload());
        return "download_form";
    }

    @RequestMapping(value = "/direct_upload_form", method = RequestMethod.GET)
    public String directUploadPhotoForm(ModelMap model) {
        model.addAttribute("photoUpload", new PhotoUpload());
        model.addAttribute("unsigned", false);
        return "direct_upload_form";
    }
    
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/direct_unsigned_upload_form", method = RequestMethod.GET)
    public String directUnsignedUploadPhotoForm(ModelMap model) throws Exception {
        model.addAttribute("photoUpload", new PhotoUpload());
        model.addAttribute("unsigned", true);
        //Cloudinary cld = Singleton.getCloudinary();
        
        Cloudinary cld = new Cloudinary(ObjectUtils.asMap(
        		  "cloud_name", "dvfaswrrp",
        		  "api_key", "675917927998788",
        		  "api_secret", "OeEUqtZ1qHfytHqcfWA8ohPeoN8"));
        
        String preset = "sample_" + cld.apiSignRequest(ObjectUtils.asMap("api_key", cld.config.apiKey), cld.config.apiSecret).substring(0, 10);
        model.addAttribute("preset", preset);
        try {
        	Singleton.getCloudinary().api().createUploadPreset(ObjectUtils.asMap(
        			"name", preset, 
        			"unsigned", true,
        			"folder", "preset_folder"));
        } catch (Exception e) {
        }
        return "direct_upload_form";
    }
    
    @RequestMapping(value = "/traceDownload", method = RequestMethod.GET)
	public ModelAndView traceFailDown() {
		
		List<String> fileLst = new ArrayList<String>();
		String rootPath = System.getProperty("catalina.home");
		
		final File folder = new File(rootPath + File.separator + "tmpFiles" + File.separator + "FailDownloadImage");
		fileLst = CSVUtils.listFilesForFolder(folder);
		
		ModelAndView model = new ModelAndView();
		model.setViewName("traceDownload");
		//model.addObject("msg", name);
		model.addObject("fileList", fileLst);

		return model;

	}
    
    @RequestMapping(value = "/traceUpload", method = RequestMethod.GET)
	public ModelAndView traceFailUp() {
		
		List<String> fileLst = new ArrayList<String>();
		String rootPath = System.getProperty("catalina.home");
		
		final File folder = new File(rootPath + File.separator + "tmpFiles" + File.separator + "FailUploadImage");
		fileLst = CSVUtils.listFilesForFolder(folder);
		
		ModelAndView model = new ModelAndView();
		model.setViewName("traceUpload");
		//model.addObject("msg", name);
		model.addObject("fileList", fileLst);

		return model;

	}
	
	@RequestMapping(value = "/download/{flag:.+}/{fileName:.+}", method = RequestMethod.GET)
	public void getErrorTrace(@PathVariable("flag") int flag, @PathVariable("fileName") String name, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		ModelAndView model = new ModelAndView();
		model.setViewName("trace");
		model.addObject("msg", name);
		
		String rootPath = System.getProperty("catalina.home");
		File fileDown;
		
		if (flag == 1) {
			fileDown = new File(rootPath + File.separator + "tmpFiles" + File.separator + "FailDownloadImage" + File.separator +  name);
		} else if (flag == 2) {
			fileDown = new File(rootPath + File.separator + "tmpFiles" + File.separator + "FailUploadImage" + File.separator +  name);
		} else {
			return ;
		}
		
		if(!fileDown.exists()){
			throw new ServletException("File doesn't exists on server.");
		}
		System.out.println("File location on server::" + fileDown.getAbsolutePath());
		//ServletContext ctx = getServletContext();
		ServletOutputStream os = null;
		InputStream fis = null;
		try {
			fis = new FileInputStream(fileDown);
			//String mimeType = ctx.getMimeType(fileDown.getAbsolutePath());
			response.setContentType("application/octet-stream");
			response.setContentLength((int) fileDown.length());
			response.setHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");
	
			os = response.getOutputStream();
			byte[] bufferData = new byte[1024];
			int read=0;
			while((read = fis.read(bufferData))!= -1){
				os.write(bufferData, 0, read);
			}
			
		} catch (Exception ex) {
			System.out.println("Error download file.");
		} finally {
			os.flush();
			os.close();
			fis.close();
		}

		//return model;

	}
	
	public void sendMail(String title, String opt, List<String> filePath) throws IOException, URISyntaxException {
		
		final String username = messageSource.getMessage("email.send.adress",null,null);
		final String password = messageSource.getMessage("email.send.password",null,null);

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		  });

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("from-email@gmail.com"));
			
			message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(messageSource.getMessage("email.recipient",null,null)));
			message.setSubject(new Formatter().format(messageSource.getMessage("email.title",null,null), title).toString());

			// Create a multipar message
	        Multipart multipart = new MimeMultipart();
			// Create the message part
	        BodyPart messageBodyPart = new MimeBodyPart();

	         // Now set the actual message
			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//dd/MM/yyyy
			Date now = new Date();
			String strDate = sdfDate.format(now);
			String emailBody = "Process status \r\n";
			emailBody = emailBody + "Process Date : " + strDate + "\r\n"; 
			emailBody = emailBody + opt;
	         
	        messageBodyPart.setText(emailBody);
	        multipart.addBodyPart(messageBodyPart);
	         
	        for (int i=0; i < filePath.size(); i++) {
				 Path p = Paths.get(filePath.get(i));
				 String fileName = p.getFileName().toString();
				 
				 messageBodyPart = new MimeBodyPart();
				 DataSource source = new FileDataSource(filePath.get(i));
				 messageBodyPart.setDataHandler(new DataHandler(source));
				 messageBodyPart.setFileName(fileName);
				 multipart.addBodyPart(messageBodyPart);
	        }

	         // Send the complete message parts
	         message.setContent(multipart);
			
			Transport.send(message);

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
}
