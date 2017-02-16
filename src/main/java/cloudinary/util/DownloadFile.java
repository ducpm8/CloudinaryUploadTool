package cloudinary.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

public class DownloadFile {
	public void downloadFile(List<String> listImage, String imageSubPath) throws IOException {
		
		// Creating the directory to store file
		
		File dirSub = new File(imageSubPath);
		File subFolder;
		
		if (!dirSub.exists()) {
			dirSub.mkdir();
			
			//1 folder
			subFolder = new File(imageSubPath + File.separator + "Photo 1");
			subFolder.mkdir();
			
			//2-4 folder
			subFolder = new File(imageSubPath + File.separator + "Photo 2-4");
			subFolder.mkdir();
			
			//5+ folder
			subFolder = new File(imageSubPath + File.separator + "Photo 5+");
			subFolder.mkdir();
		}
		
		URL website;
		ReadableByteChannel rbc;
		String imageName;
		String imageLink;
		
		String nameOnly = "";
		String extension = "";
		
		for (int i=0; i<listImage.size(); i++) {
			imageLink = listImage.get(i);
			imageName = imageLink.substring(imageLink.lastIndexOf("/")+1,imageLink.length());
			
			int pos = imageName.lastIndexOf(".");
			if (pos > 0) {
				nameOnly = imageName.substring(0, pos);
				extension = imageName.substring(pos, imageName.length());
				imageName = nameOnly + (i+1) + extension;
			} else {
				imageName = imageName + (i+1);
			}
			
			website = new URL(imageLink);
			rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos = new FileOutputStream(imageSubPath + File.separator  + imageName);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			
			fos.close();
		}
	}
}
