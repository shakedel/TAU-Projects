package tests;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import main.Main;

import org.junit.Test;

public class RunTest {
	
	@Test
	public void testAllDirs() throws IOException {
		File tempDir = Files.createTempDirectory(Paths.get("work"), "tempOut").toFile();
		
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
			tempDir.delete();
		}
	}
	
	public void testDir(File inDir, File outDir) throws IOException {
		File cfgFile = new File(inDir, "cfg.txt");
		File memInFile = new File(inDir, "memin.txt");
		
		File memOutFile = new File(outDir, "memout.txt");
		File regOut0File = new File(outDir, "regout0.txt");
		File regOut1File = new File(outDir, "regout1.txt");
		File trace0File = new File(outDir, "trace0.txt");
		File trace1File = new File(outDir, "trace1.txt");
		File cpi0File = new File(outDir, "cpi0.txt");
		File cpi1File = new File(outDir, "cpi1.txt");
		
		new Main(cfgFile, memInFile, memOutFile, regOut0File, trace0File, cpi0File, regOut1File, trace1File, cpi1File);
		assert true;
	}
}
