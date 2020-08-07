package org.sdrc.fani.service;

import java.util.ArrayList;
import java.util.List;

import org.sdrc.fani.models.SVGModel;
import org.springframework.stereotype.Component;

@Component
public class SVGJSONModel {
	
	public static List<SVGModel> getSvgModel(){
		
		
		List<SVGModel> listOfSvgModel = new ArrayList<SVGModel>();
		SVGModel svgModel =null;
		
		svgModel = new SVGModel();
		svgModel.setIndicatorGroupName("Gr6");
		svgModel.setSvg("<svg id=\"chart\" width=\"487.156\" height=\"330\" transform=\"translate(4.0008,-20)\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\"><g transform=\"translate(183.578,145)\"><g class=\"arc\" align=\"left\"><path d=\"M0.34663525451791566,-121.95408985545839A3,3,0,0,1,3.4289295640552417,-124.95296091747787A125,125,0,0,1,29.93673081501057,-121.36223526332067A3,3,0,0,1,32.11013785753788,-117.65143108030095L16.402513401918114,-60.73613210085731A3,3,0,0,1,12.867261733171954,-58.60404060719746A60,60,0,0,0,3.187271670969431,-59.91528435462387A3,3,0,0,1,0.3466352545179068,-62.911048572355064Z\" style=\"cursor: pointer; fill: rgb(9, 64, 134);\"></path><text transform=\"translate(12.416577088133138,-91.66285296353396)\" dx=\"-0.5em\" dy=\"0.5em\" style=\"color: rgb(255, 255, 255);\">3</text></g><g class=\"arc\" align=\"left\"><path d=\"M33.2546056958344,-120.49535758698165L15.96221073400051,-57.837771641751196Z\" style=\"cursor: default; fill: rgb(58, 116, 185);\"></path><text transform=\"translate(24.608408214917453,-89.16656461436642)\" dx=\"-0.5em\" dy=\"0.5em\" style=\"color: rgb(255, 255, 255);\"></text></g><g class=\"arc\" align=\"left\"><path d=\"M32.77842488066412,-117.46699558095266A3,3,0,0,1,36.54745231839311,-119.53779205353753A125,125,0,1,1,-76.21956994740664,-99.07359465080687A3,3,0,0,1,-71.96324928554402,-98.45918413606495L-37.25862036884023,-50.69236033845274A3,3,0,0,1,-37.795877478061975,-46.599051982452686A60,60,0,1,0,19.01208476869704,-56.90817720457999A3,3,0,0,1,17.07080042504434,-60.55169660150903Z\" style=\"cursor: pointer; fill: rgb(187, 187, 187);\"></path><text transform=\"translate(16.516512768873895,91.01348694537324)\" dx=\"-0.5em\" dy=\"0.5em\" style=\"color: rgb(255, 255, 255);\">60</text></g><g class=\"arc\" align=\"left\"><path d=\"M-73.47315653655916,-101.12712429686842L-35.267115137548394,-48.54101966249684Z\" style=\"cursor: default; fill: rgb(103, 172, 244);\"></path><text transform=\"translate(-54.37013583705377,-74.83407197968263)\" dx=\"-0.5em\" dy=\"0.5em\" style=\"color: rgb(255, 255, 255);\"></text></g><g class=\"arc\" align=\"left\"><path d=\"M-71.40238166203511,-98.86667831712545A3,3,0,0,1,-70.67144536773593,-103.10454310860949A125,125,0,0,1,-3.4289295640552395,-124.95296091747785A3,3,0,0,1,-0.3466352545179032,-121.95408985545839L-0.3466352545179121,-62.911048572355064A3,3,0,0,1,-3.18727167096944,-59.91528435462387A60,60,0,0,0,-32.63876358305375,-50.34591454894353A3,3,0,0,1,-36.6977527453313,-51.099854519513286Z\" style=\"cursor: pointer; fill: rgb(98, 97, 194);\"></path><text transform=\"translate(-28.58407197968265,-87.9727277573017)\" dx=\"-0.5em\" dy=\"0.5em\" style=\"color: rgb(255, 255, 255);\">7</text></g><g class=\"arc\" align=\"left\"><path d=\"M-2.2962127484012872e-14,-125L-1.1021821192326178e-14,-60Z\" style=\"cursor: default; fill: rgb(166, 204, 230);\"></path><text transform=\"translate(-1.6991974338169524e-14,-92.5)\" dx=\"-0.5em\" dy=\"0.5em\" style=\"color: rgb(255, 255, 255);\"></text></g></g></svg>");
		svgModel.setChartType("donut");
		svgModel.setShowNName("Gr6");
		svgModel.setIndName("Distribution of participants by Social Category");
		listOfSvgModel.add(svgModel);
		
		
		svgModel = new SVGModel();
		svgModel.setIndicatorGroupName("Gr9");
		svgModel.setSvg("<svg id=\"chart\" width=\"487.156\" height=\"330\" transform=\"translate(4.0008,-20)\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\"><g transform=\"translate(183.578,145)\"><text transform=\"translate(3.578000000000003,-35)\" x=\"0\" y=\"30\" font-size=\"20px\" style=\"text-anchor: middle;\">Data Not Available</text></g></svg>");
		svgModel.setChartType("donut");
		svgModel.setShowNName("Gr9");
		svgModel.setIndName("Distribution of participants by Social Category");
		listOfSvgModel.add(svgModel);

		return listOfSvgModel;
		
	}

}
