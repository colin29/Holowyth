package com.mygdx.holowyth.test.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;

public class ResizeImages {
	
	public static void main(String[] args) throws IOException{
		
		String fileName = "img\\witch.png";
		File image = new File(fileName);
		
		String[] splt = fileName.split("\\.");
		String originalPath = splt[0];
		String fileExtension = splt[1];
		
		BufferedImage img = ImageIO.read(image); // load image
		//resize to 150 pixels max
		BufferedImage scaledImage = Scalr.resize(img, Scalr.Method.QUALITY,  Mode.AUTOMATIC, 60, 100);
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		File destFile = new File(originalPath + "resized" + "." + fileExtension);
		ImageIO.write(scaledImage, fileExtension, destFile);
	}

}
