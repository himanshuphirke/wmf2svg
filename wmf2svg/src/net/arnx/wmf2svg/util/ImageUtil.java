package net.arnx.wmf2svg.util;

import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBuffer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.api.images.ImagesService.OutputEncoding;

public class ImageUtil {
	private static final int API_TYPE_NONE = -1;
	private static final int API_TYPE_IMAGEIO = 0;
	private static final int API_TYPE_GAE = 1;
	
	private static int apiType = API_TYPE_NONE;
	
	static {
		if (apiType == API_TYPE_NONE) {
			try {
				Class.forName("javax.imageio.ImageIO");
				apiType = API_TYPE_IMAGEIO;
			} catch (ClassNotFoundException e) {
				// no handle
			}
		}
		
		if (apiType == API_TYPE_NONE) {
			try {
				Class.forName("com.google.appengine.api.images.Image");
				apiType = API_TYPE_GAE;
			} catch (ClassNotFoundException e) {
				// no handle
			}
		}
	}
	
	public static byte[] convert(byte[] image, String destType, boolean reverse) {
		if (destType == null) {
			throw new IllegalArgumentException("dest type is null.");
		} else {
			destType = destType.toLowerCase();
		}
		
		if (apiType == API_TYPE_NONE) {
			throw new UnsupportedOperationException("Image Conversion API(Image IO or GAE Image API) is missing.");
		}
		
		byte[] outimage = null;
		
		if (apiType == API_TYPE_GAE) {
			ImagesService.OutputEncoding encoding = null;
			if ("png".equals(destType)) {
				encoding = OutputEncoding.PNG;
			} else if ("jpeg".equals(destType)) {
				encoding = OutputEncoding.JPEG;
			} else {
				throw new UnsupportedOperationException("unsupported image encoding: " + destType);
			}
			
			ImagesService imagesService = ImagesServiceFactory.getImagesService();
			Image bmp = ImagesServiceFactory.makeImage(image);
			
			Transform t = (reverse) ? ImagesServiceFactory.makeVerticalFlip() : null;			
			outimage = imagesService.applyTransform(t, bmp, encoding).getImageData();
		} else {
			try {
				// convert to 24bit color
				BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));
				BufferedImage dst = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
				ColorConvertOp colorConvert = new ColorConvertOp(dst.getColorModel().getColorSpace(), null);
				colorConvert.filter(bufferedImage, dst);
				bufferedImage = dst;
				
				if (reverse) {
					DataBuffer srcData = bufferedImage.getRaster().getDataBuffer();
					BufferedImage dstImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());
					DataBuffer dstData = dstImage.getRaster().getDataBuffer();
					int lineSize = bufferedImage.getWidth() * bufferedImage.getColorModel().getPixelSize() / 8;
					for (int h = 0, k = bufferedImage.getHeight() - 1; h < bufferedImage.getHeight(); h++, k--) {
						for (int j = 0; j < lineSize; j++) {
							dstData.setElem(h * lineSize + j, srcData.getElem(k * lineSize + j));
						}
					}
					bufferedImage = dstImage;
				}
				
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ImageIO.write(bufferedImage, destType, out);
				outimage = out.toByteArray();
			} catch (IOException e) {
				// never occurred.
			}
		}
		
		return null;
	}
	
}
