package email.reader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

@Service
public class QRExtractor {
	private static final Logger logger = LoggerFactory.getLogger(QRExtractor.class);

	String readQRCode(String filePath) throws IOException, NotFoundException {

		BufferedImage image = ImageIO.read(new File(filePath));
		LuminanceSource source = new BufferedImageLuminanceSource(image);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

		Reader reader = new MultiFormatReader();
		Result result = null;
		String QRCodeURL = null;
		try {
			result = reader.decode(bitmap);
			logger.debug("The file is a QR Code");
			QRCodeURL = new QRExtractor().readQRCode(filePath);
			if (QRCodeURL != null) {
				logger.debug("QR Code URL : " + QRCodeURL);
			}
		} catch (NotFoundException e) {

			logger.error("File not Found " + e.getCause());
		} catch (ChecksumException e) {

			logger.error("The file is a not a QR Code " + e.getCause());
		} catch (FormatException e) {
			logger.error("The file is a not a QR Code " + e.getCause());
		}
		return (String) result.getText();
	}

}