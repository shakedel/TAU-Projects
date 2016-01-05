package tests;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;
import main.Main;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class RunTest {
	
	@Test
	public void testAllDirs() throws IOException {
		DateFormat df = new SimpleDateFormat("HH-mm");
		File tempDir = Files.createTempDirectory(Paths.get("work/temp"), "out_"+df.format(new Date())+"_").toFile();
		
		try {
			URL url = RunTest.class.getResource("/resources/");
			File resourcesDir;
			try {
			  resourcesDir = new File(url.toURI());
			} catch(URISyntaxException e) {
			  resourcesDir = new File(url.getPath());
			}
			
			for (File dir: resourcesDir.listFiles()) {
				File outDir = new File(tempDir, dir.getName());
				testDir(dir, outDir);
			}
		} finally {
			FileUtils.deleteDirectory(tempDir);
		}
	}
	
	public void testDir(File inDir, File outDir) throws IOException {
		File cfgFile = new File(inDir, "cfg.txt");
		File memInFile = new File(inDir, "memin.txt");
		
		File expectedMemOutFile = new File(inDir, "memout.txt");
		File expectedRegOut0File = new File(inDir, "regout0.txt");
		File expectedRegOut1File = new File(inDir, "regout1.txt");
		File expectedTrace0File = new File(inDir, "trace0.txt");
		File expectedTrace1File = new File(inDir, "trace1.txt");
		File expectedCpi0File = new File(inDir, "cpi0.txt");
		File expectedCpi1File = new File(inDir, "cpi1.txt");
		
		File memOutFile = new File(outDir, "memout.txt");
		File regOut0File = new File(outDir, "regout0.txt");
		File regOut1File = new File(outDir, "regout1.txt");
		File trace0File = new File(outDir, "trace0.txt");
		File trace1File = new File(outDir, "trace1.txt");
		File cpi0File = new File(outDir, "cpi0.txt");
		File cpi1File = new File(outDir, "cpi1.txt");
		
		new Main(cfgFile, memInFile, memOutFile, regOut0File, trace0File, cpi0File, regOut1File, trace1File, cpi1File);
		
		compareFiles(expectedMemOutFile, memOutFile);
		compareFiles(expectedRegOut0File, regOut0File);
		compareFiles(expectedRegOut1File, regOut1File);
		compareFiles(expectedTrace0File, trace0File);
		compareFiles(expectedTrace1File, trace1File);
		compareFiles(expectedCpi0File, cpi0File);
		compareFiles(expectedCpi1File, cpi1File);
		
	}
	
	private void compareFiles(File expected, File collected) throws IOException {
		String expectedStr = FileUtils.readFileToString(expected, "utf-8");
		String collectedStr = FileUtils.readFileToString(collected, "utf-8");
		try {
			Assert.assertEquals(collected.getName()+": expected differs from collected!", expectedStr, collectedStr); 
		} catch (AssertionError e) {
			throw e;
		}
		
	}
}
