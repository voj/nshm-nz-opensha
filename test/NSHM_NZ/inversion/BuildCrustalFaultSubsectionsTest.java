package NSHM_NZ.inversion; // is it the right package - inversion is still far away  

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
 * Objective: Examine how the subsections are build.
 * 
 * Fault sections are subdivided such that subsections have length at least(or is it at most?) 0.5 of the seismogenic width - complete down-dip width DDW. 
 * DDW is not a measure of vertical depth, but of rupture extent that encompasses DDW. Fault-dip angle have an key role.
 * 
 * Questions:
 * (1) Are width of each subsection appropriately computed (whether do 2 subsections ensures L~W). 
 * (2) How are exceptions (such as fault-sections with length shorter than seismogenic width handled) handled.
 *          
 * @author Thingbaijam        
 * 
 * Note to myself:
 * 
 * getSubSectionsList() is declared (overloaded) in the interface FaultSection and implemented in class FaultSectionPrefData.
 * Class DeformationModelFetcher also has a method of same name, which ultimately can be connected to the implementation in FaultSectionPrefData.
 * 
 *    
*/


public class BuildCrustalFaultSubsectionsTest {

	private static final double grid_disc = 5d;

	
	static List<FaultSection> fsd;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// this is the input fault section data file
		// Still use alderman_sections.xml
		
		File fsdFile = new File("./test/NSHM_NZ/inversion/fixtures/alderman_sections.xml");
		
		
		// load in the fault section data ("parent sections")
		// Note2self: Is this natural way to load the fault sections (or fault models). See class ClusterRuptureBuilder to understand better
		// Likewise, hard-codes in emums FaultModels, DeformationModels, of course - these are in package UCERF3. 
		// perhaps, a work-sheet could be use to describe/read in the models  
		fsd = FaultModels.loadStoredFaultSections(fsdFile);
	}
	
    @Test 
	public void testSubsection() throws IOException {
		
		System.out.println(fsd.size() + " Crustal Fault Sections");
		// this list will store our subsections
		List<FaultSection> subSections = Lists.newArrayList();
		
		// sub section length factor (in terms of DDW)
		double SubSectionLengthFactor = 0.5;

		//  Build and inspect the subsections 
		int sectIndex = 0;
		
		for (FaultSection pSect : this.fsd) {
			
			double ddw = pSect.getOrigDownDipWidth();
			
			double maxSectLength = ddw*SubSectionLengthFactor; // why is this the maximum?
			
			// the "2" here sets a minimum number of sub sections
			List<? extends FaultSection> newSubSects = pSect.getSubSectionsList(maxSectLength, sectIndex, 1);
			
			// Note2myself: fundamentally, this computation is done by getEqualLengthSubsectionTraces() defined in class FaultUtils  
			System.out.println("Fault Section = " + pSect.getName() + " ------------------");
			System.out.println("0.5 DDW = "+ 0.5*ddw);
			
			
			// Two ways by which we can retrieve the length of a subsection, which provide approximately equal lengths. 
			// I assume that a Fault Trace is a straight line. 
			// double lengthOfSubSection =  pSect.getFaultTrace().getTraceLength()/newSubSects.size();
			// System.out.println("Length of subsection by division of the fault trace = " 
			//			+ lengthOfSubSection);
			
			double lengthOfSubSection = newSubSects.get(1).getFaultTrace().getTraceLength();
			
			System.out.println("Length of subsection from the subsection = " + lengthOfSubSection);
			
			assertEquals(Boolean.TRUE,lengthOfSubSection>=0.5*ddw);
			// if it fails, length of at least two subsections may be smaller than DDW   
			
			subSections.addAll(newSubSects);
			sectIndex += newSubSects.size();
			
		}
    }
}




