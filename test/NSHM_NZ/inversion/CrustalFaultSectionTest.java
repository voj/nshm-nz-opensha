package NSHM_NZ.inversion;

import static org.junit.Assert.*;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentException;
import org.junit.BeforeClass;
import org.junit.Test;

import org.opensha.commons.data.CSVFile;
import org.opensha.commons.geo.Location;
import org.opensha.commons.util.IDPairing;
import org.opensha.commons.util.XMLUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.FaultSection;
import org.opensha.sha.faultSurface.FaultTrace;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.ScalingRelationships;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
import scratch.UCERF3.inversion.coulomb.CoulombRates;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.inversion.laughTest.UCERF3PlausibilityConfig;
import scratch.UCERF3.inversion.SectionCluster;
import scratch.UCERF3.inversion.SectionClusterList;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.FaultSystemIO;

import scratch.UCERF3.inversion.UCERF3SectionConnectionStrategy;

/*
 * discarded ---
*/



public class CrustalFaultSectionTest {

	private static final double grid_disc = 5d;

	
	static List<FaultSection> fsd;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// this is the input fault section data file
		File fsdFile = new File("./test/NSHM_NZ/inversion/fixtures/alderman_sections.xml");
		// load in the fault section data ("parent sections")
		fsd = FaultModels.loadStoredFaultSections(fsdFile);
	}
	
	@Test
	public void testXMLSectionsLoaded() throws IOException {
		// rake value/s should have no effect on output without plausibility filter 
		
		assertEquals(16, fsd.size());
		
	}
	
    @Test 
	public void testRuptureGeneratorSetup() throws IOException {

		
		System.out.println(fsd.size() + " Crustal Fault Sections");

		// directory to write output files
		File outputDir = new File("/tmp");
		// maximum sub section length (in units of DDW)
		double maxSubSectionLength = 0.5;
		// max distance for linking multi fault ruptures, km
		double maxDistance = 10d;

		// write crustal section data to file
		File subductionSectDataFile = new File(outputDir, "crustal_sections.xml");
		Document doc0 = XMLUtils.createDocumentWithRoot();
		FaultSystemIO.fsDataToXML(doc0.getRootElement(), FaultModels.XML_ELEMENT_NAME, null, null, fsd);
		XMLUtils.writeDocumentToFile(subductionSectDataFile, doc0);

		// calculate distances between each subsection
		Map<IDPairing, Double> subSectionDistances = DeformationModelFetcher.calculateDistances(maxDistance, fsd);
		Map<IDPairing, Double> reversed = Maps.newHashMap();
        
		//Dump distances		
		for(IDPairing pair : subSectionDistances.keySet()) {
			System.out.println("pair: "+pair+ ";  dist: "+subSectionDistances.get(pair));
		}
		assertEquals(1,2);
		
//		// now add the reverse distance
//		for (IDPairing pair : subSectionDistances.keySet()) {
//			IDPairing reverse = pair.getReversed();
//			reversed.put(reverse, subSectionDistances.get(pair));
//		}		
//		subSectionDistances.putAll(reversed);
//		
//		// sizes vs minsections in rupure: 9 => 1, 8 -> (1+2) , 7 => (1+2+3). 6 => (1+2+3+4)
//		assertEquals(1, getRuptures(getSectionClusterList(subSections, subSectionDistances, 9)).size()); 
//		assertEquals(3, getRuptures(getSectionClusterList(subSections, subSectionDistances, 8)).size());
//		assertEquals(6, getRuptures(getSectionClusterList(subSections, subSectionDistances, 7)).size());
//		assertEquals(10, getRuptures(getSectionClusterList(subSections, subSectionDistances, 6)).size());
//		assertEquals(15, getRuptures(getSectionClusterList(subSections, subSectionDistances, 5)).size());
//		
//		SectionClusterList clusters = getSectionClusterList(subSections, subSectionDistances, 6);
//		List<List<Integer>> ruptures = getRuptures(getSectionClusterList(subSections, subSectionDistances, 6));
//
//		// write rupture/subsection associations to file
//		// format: rupID	sectID1,sectID2,sectID3,...,sectIDN
//		File rupFile = new File(outputDir, "ruptures.txt");
//		FileWriter fw = new FileWriter(rupFile);
//		Joiner j = Joiner.on(",");
//		for (int i=0; i<ruptures.size(); i++) {
//			fw.write(i+"\t"+j.join(ruptures.get(i))+"\n");
//		}
//		fw.close();
//
//
//		// build actual rupture set for magnitudes and such
//		FaultModels fm = null;
//		LogicTreeBranch branch = LogicTreeBranch.fromValues(fm, 
//			DeformationModels.GEOLOGIC,
//				ScalingRelationships.SHAW_2009_MOD, SlipAlongRuptureModels.TAPERED);
//		InversionFaultSystemRupSet rupSet = new InversionFaultSystemRupSet(branch, clusters, subSections);
//		
//		File zipFile = new File(outputDir, "rupSet.zip");
//		FaultSystemIO.writeRupSet(rupSet, zipFile);


	}
	
	
//	private SectionClusterList getSectionClusterList(List<FaultSection> subSections, Map<IDPairing, Double> subSectionDistances, int minSectionsInRupture) {
//		/*
//		 * test help function  
//		*/
//		Map<IDPairing, Double> sectionAzimuths = Maps.newHashMap(); //just an empty list, since we're not using azimuths now
//
//		// instantiate our laugh test filter
//		UCERF3PlausibilityConfig laughTest = UCERF3PlausibilityConfig.getDefault();
//
//		// laughTest.setMaxCmlmJumpDist(5d); 	// has no effect here as it's a junction only test
//		// laughTest.setMaxJumpDist(2d); 		// looks like this might only impact (parent) section jumps
//		laughTest.setMaxAzimuthChange(Double.MAX_VALUE); // azimuth change constraints makes no sense with 2 axes 
//		laughTest.setMaxCmlAzimuthChange(Double.MAX_VALUE);
//		laughTest.setMinNumSectInRup(minSectionsInRupture); 		
//
//		//disable our coulomb filter as it uses a data file specific to SCEC subsections
//		CoulombRates coulombRates  = null;
//		laughTest.setCoulombFilter(null);
//
//		// A section connection strategy is now required, let's use the default UCERF3...
//		UCERF3SectionConnectionStrategy connectionStrategy = new UCERF3SectionConnectionStrategy(
//				laughTest.getMaxAzimuthChange(), coulombRates);
//
//		// this separates the sub sections into clusters which are all within maxDist of each other and builds ruptures
//		// fault model and deformation model here are needed by InversionFaultSystemRuptSet later, just to create a rup set
//		// zip file
//
//		// SectionClusterList clusters = new SectionClusterList(
//		// 		fm, DeformationModels.GEOLOGIC, laughTest, coulombRates, subSections, subSectionDistances, sectionAzimuths);
//		SectionClusterList clusters = new SectionClusterList(
//				connectionStrategy, laughTest, subSections, subSectionDistances, sectionAzimuths);
//		return clusters;
//
//	}
//
//    private List<List<Integer>> getRuptures(SectionClusterList clusters ) {
//    	List<List<Integer>> ruptures = Lists.newArrayList();
//		for (SectionCluster cluster : clusters) {
//			System.out.println("cluster "+cluster);// + " : " + cluster.getNumRuptures());
//			ruptures.addAll(cluster.getSectionIndicesForRuptures());
//		}		
//		System.out.println("Created "+ruptures.size()+" ruptures");
//		return ruptures;
//    }
//    
//
//	@Test
//	public void testRuptureGeneratorSetup() throws IOException {
//		InputStream csvdata = this.getClass().getResourceAsStream("fixtures/patch_4_10.csv");
//		CSVFile<String> csv = CSVFile.readStream(csvdata, false);
//		
//		FaultSectionPrefData parentSection = new FaultSectionPrefData();
//		parentSection.setSectionId(100);
//		parentSection.setSectionName("ParentSection 100");
//
//		List<FaultSection> subSections = Lists.newArrayList();
//		for (int row=1; row<csv.getNumRows(); row++) {
//			FaultSection fs = buildFaultSectionFromCsvRow(row-1, csv.getLine(row));
//			fs.setParentSectionId(parentSection.getSectionId());
//			fs.setParentSectionName(parentSection.getSectionName());
//			subSections.add(fs);
//		}
//		
//		System.out.println(subSections.size() + " Subduction Fault Sections");
//
//		// directory to write output files
//		File outputDir = new File("/tmp");
//		// maximum sub section length (in units of DDW)
//		double maxSubSectionLength = 0.5;
//		// max distance for linking multi fault ruptures, km
//		double maxDistance = 10d;
//
//		// write subduction section data to file
//		File subductionSectDataFile = new File(outputDir, "subduction_sections.xml");
//		Document doc0 = XMLUtils.createDocumentWithRoot();
//		FaultSystemIO.fsDataToXML(doc0.getRootElement(), FaultModels.XML_ELEMENT_NAME, null, null, subSections);
//		XMLUtils.writeDocumentToFile(subductionSectDataFile, doc0);
//
//		// calculate distances between each subsection
//		Map<IDPairing, Double> subSectionDistances = DeformationModelFetcher.calculateDistances(maxDistance, subSections);
//		Map<IDPairing, Double> reversed = Maps.newHashMap();
//
//		//Dump distances		
//		for(IDPairing pair : subSectionDistances.keySet()) {
//			System.out.println("pair: "+pair+ ";  dist: "+subSectionDistances.get(pair));
//		}
//
//		// now add the reverse distance
//		for (IDPairing pair : subSectionDistances.keySet()) {
//			IDPairing reverse = pair.getReversed();
//			reversed.put(reverse, subSectionDistances.get(pair));
//		}		
//		subSectionDistances.putAll(reversed);
//		
//		// sizes vs minsections in rupure: 9 => 1, 8 -> (1+2) , 7 => (1+2+3). 6 => (1+2+3+4)
//		assertEquals(1, getRuptures(getSectionClusterList(subSections, subSectionDistances, 9)).size()); 
//		assertEquals(3, getRuptures(getSectionClusterList(subSections, subSectionDistances, 8)).size());
//		assertEquals(6, getRuptures(getSectionClusterList(subSections, subSectionDistances, 7)).size());
//		assertEquals(10, getRuptures(getSectionClusterList(subSections, subSectionDistances, 6)).size());
//		assertEquals(15, getRuptures(getSectionClusterList(subSections, subSectionDistances, 5)).size());
//		
//		SectionClusterList clusters = getSectionClusterList(subSections, subSectionDistances, 6);
//		List<List<Integer>> ruptures = getRuptures(getSectionClusterList(subSections, subSectionDistances, 6));
//
//		// write rupture/subsection associations to file
//		// format: rupID	sectID1,sectID2,sectID3,...,sectIDN
//		File rupFile = new File(outputDir, "ruptures.txt");
//		FileWriter fw = new FileWriter(rupFile);
//		Joiner j = Joiner.on(",");
//		for (int i=0; i<ruptures.size(); i++) {
//			fw.write(i+"\t"+j.join(ruptures.get(i))+"\n");
//		}
//		fw.close();
//
//
//		// build actual rupture set for magnitudes and such
//		FaultModels fm = null;
//		LogicTreeBranch branch = LogicTreeBranch.fromValues(fm, 
//			DeformationModels.GEOLOGIC,
//				ScalingRelationships.SHAW_2009_MOD, SlipAlongRuptureModels.TAPERED);
//		InversionFaultSystemRupSet rupSet = new InversionFaultSystemRupSet(branch, clusters, subSections);
//		
//		File zipFile = new File(outputDir, "rupSet.zip");
//		FaultSystemIO.writeRupSet(rupSet, zipFile);
//
//
//	}
//	
//
//	@Test
//	public void testTempo() throws IOException {
//		// rake value/s should have no effect on output without plausibility filter 
//		assertEquals(2, 2);
//		
//	}


	
}