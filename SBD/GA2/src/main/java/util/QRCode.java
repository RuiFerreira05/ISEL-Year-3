package util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.ByteMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRCode {
	private static int size=250;
	private static Color bgColor = Color.BLACK; // Color.RED; 	 
	private static Color ikColor = Color.WHITE; // Color.GREEN; 

	public static void main(String[] args) throws WriterException, IOException {
		String msgText = "bom dia!";
		displayQR(msgText);
		System.out.println("avançou");
		createQR("sauda.png", msgText);
	}

	public static void displayQR(String qrCodeText) throws WriterException {
		display(generateQR(qrCodeText));
	}
	
	private static void display(BufferedImage image) {
		JFrame frame = new JFrame();
		frame.setTitle("Display");
		frame.setSize(image.getWidth(), image.getHeight());
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);   // WindowConstants.EXIT_ON_CLOSE
		JLabel label = new JLabel();
		label.setIcon(new ImageIcon(image));
		frame.getContentPane().add(label, BorderLayout.CENTER);
		frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setVisible(true);
	}

	public static void createQR(String filePath, String qrCodeText) throws IOException, WriterException {
		create(filePath, generateQR(qrCodeText));
	}
	
	private static void create(String filePath, BufferedImage img) throws IOException {
		String fileType = filePath.substring(filePath.indexOf('.')+1);
		ImageIO.write(img, fileType, new File(filePath));
	}

	private static BufferedImage generateQR(String qrCodeText) throws WriterException {
		return generateQR(qrCodeText, size, bgColor, ikColor);
	}
	
	private static BufferedImage generateQR(String qrCodeText, int size, Color bgColor, Color ikColor) throws WriterException {
		// Create the ByteMatrix for the QR-Code that encodes the given String
		Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		ByteMatrix byteMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, size, size, hintMap);

		// Make the BufferedImage that are to hold the QRCode
		int matrixWidth = byteMatrix.getWidth();
		BufferedImage image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
		image.createGraphics();

		Graphics2D graphics = (Graphics2D) image.getGraphics();
		graphics.setColor(ikColor);
		graphics.fillRect(0, 0, matrixWidth, matrixWidth);

		// Paint and save the image using the ByteMatrix
		graphics.setColor(bgColor);

		for (int i = 0; i < matrixWidth; i++) {
			for (int j = 0; j < matrixWidth; j++) {
				if (byteMatrix.get(i, j) != 0) {
					graphics.fillRect(i, j, 1, 1);
				}
			}
		}
		return image;
	}
}