package email.reader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

@Service
public class QRExtractor {
	private static final Logger logger = LoggerFactory.getLogger(QRExtractor.class);

	List<String> readQRCode(String filePath) throws IOException, NotFoundException {
		List<String> qrCodeUrlList = new ArrayList<String>();
		BufferedImage bufferedImage = ImageIO.read(new File(filePath));
		BinaryBitmap binaryBitmap = new BinaryBitmap(
				new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage)));
		Result result = null;
		try {

			Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
			hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
			result = new MultiFormatReader().decode(binaryBitmap, hints);
			logger.debug("The file is a QR Code");
			qrCodeUrlList.add(result.getText());
			return qrCodeUrlList;
		} catch (NotFoundException e) {
			logger.error("Exception " + e.getMessage() + " Cause " + e.getCause());
		}
		return qrCodeUrlList;
	}
}