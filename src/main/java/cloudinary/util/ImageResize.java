package cloudinary.util;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;

public class ImageResize {
	private static final int IMG_WIDTH = 1800;
	private static final int IMG_HEIGHT = 1800;

	public static String doResize(String rootPath) throws IOException {
		
		CSVUtils csvUtil = new CSVUtils();
			
		List<File> imageList = Directory.listFileInFolder(rootPath);
		
		String fileName = "";
		String tmp = "";
		String imageName;
		String imageLink;
		String path="";
		
		String nameOnly = "";
		
		for (int i=0; i<imageList.size(); i++) {
			try{
				
				BufferedImage originalImage = ImageIO.read(imageList.get(i));
				int type = originalImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
				
				imageName = imageList.get(i).getName();
				path = imageList.get(i).getAbsolutePath();
				
				path = path.substring(0,path.lastIndexOf(File.separator));
				
				int pos = imageName.lastIndexOf(".");
				if (pos > 0) {
					nameOnly = imageName.substring(0, pos);
					imageName = nameOnly + "_jpg";
				} else {
					continue;
				}
		
				BufferedImage resizeImageJpg = resizeImage(originalImage, type);
				ImageIO.write(resizeImageJpg, "jpg", new File(path + File.separator + imageName + ".jpg"));
				
				imageList.get(i).delete();
		
			}catch(Exception e){
//				System.out.println(e.getMessage());
//				orderReport.setSku(StringUtils.EMPTY);
//                
//    			orderReport.setReason("Link2 null");
//    			orderReport.setLine(String.valueOf(j+1));
//    			orderReportFailList.add(orderReport);
				
				continue;
			}
		}
		
		
		
		
		
		return "";
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int type){
		BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
		g.dispose();

		return resizedImage;
    }

    private static BufferedImage resizeImageWithHint(BufferedImage originalImage, int type){

		BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
		g.dispose();
		g.setComposite(AlphaComposite.Src);
	
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,
		RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);
	
		return resizedImage;
    }
}
