package org.sdrc.fani.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;

@Service
public class ImageEncoder {

	@Autowired
	private ConfigurableEnvironment env;

	/**
	 * @author Sarita Panigrahi, created on: 23-Oct-2017
	 * @param path
	 * @param areaName
	 * @return areaName is added only for direct jpg download
	 */

	private SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyyMMddHHmmssS");

	public String createImgFromFile(String path, String align, String chartType,String downloadType) {
		// Create a JPEG transcoder

//		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
//		ConfigurableEnvironment env = context.getEnvironment();

		String filePath = env.getProperty("output.path.pdf") + "svg/" ;
//		String filePath = "C:/SIRMNCHA/svgs/";

		File filePathDirect = new File(filePath);
		if (!filePathDirect.exists())
			filePathDirect.mkdir();

		String date = timeFormatter.format(new Date());
		String fileName = "";

		fileName = filePath + "CHART_" + date + ".jpg";

		try {
			JPEGTranscoder t = new JPEGTranscoder();

			// Set the transcoding hints.
			t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(1));

			switch (align) {
			
			case "col-md-3":

				t.addTranscodingHint(JPEGTranscoder.KEY_WIDTH, new Float(240));
				t.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, new Float(100));

				break;

			case "col-md-4":

				t.addTranscodingHint(JPEGTranscoder.KEY_WIDTH, new Float(325));
				if (chartType.equals("card")) {
					
					t.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, new Float(100));
				} else {
					t.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, new Float(295));
				}

				break;

			case "col-md-6":
			 if (downloadType != null) {


					if (chartType.equals("card")) {
						t.addTranscodingHint(JPEGTranscoder.KEY_WIDTH, new Float(505));
						t.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, new Float(100));
					} else {
						t.addTranscodingHint(JPEGTranscoder.KEY_WIDTH, new Float(500));
						t.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, new Float(298));
					}
				} else {


					if (chartType.equals("card")) {
						t.addTranscodingHint(JPEGTranscoder.KEY_WIDTH, new Float(505));
						t.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, new Float(100));
					} else {
						t.addTranscodingHint(JPEGTranscoder.KEY_WIDTH, new Float(570));
						t.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, new Float(298));
					}
				}
				break;

			case "col-md-12":

				
				if(downloadType!=null) {
					t.addTranscodingHint(JPEGTranscoder.KEY_WIDTH, new Float(570));

					if (chartType.equals("card")) {
						t.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, new Float(85));
					} else {
						t.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, new Float(302));
					}
				} else {
					t.addTranscodingHint(JPEGTranscoder.KEY_WIDTH, new Float(1200));

					if (chartType.equals("card")) {
						t.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, new Float(100));
					} else {
						t.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, new Float(302));
					}
				}
//				t.addTranscodingHint(JPEGTranscoder.KEY_AOI, new Rectangle(0, 0, 600, 150));
				break;

			default:
				break;
			}

//		     t.addTranscodingHint(JPEGTranscoder.KEY_AOI, new Rectangle(50, -200, 1200, 600));

			// Create the transcoder input.
			String svgURI = new File(path).toURI().toURL().toString();

			TranscoderInput input = new TranscoderInput(svgURI);

			OutputStream ostream = new FileOutputStream(fileName);
			TranscoderOutput output = new TranscoderOutput(ostream);

			// Save the image.

			t.transcode(input, output);
			ostream.flush();
			ostream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Flush and close the stream.

		return fileName;
	}

}