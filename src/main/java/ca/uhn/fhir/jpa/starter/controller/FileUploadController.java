package ca.uhn.fhir.jpa.starter.controller;

import org.apache.jena.base.Sys;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;



import javax.servlet.annotation.MultipartConfig;

@RestController
@RequestMapping("/bpmn")
@MultipartConfig(fileSizeThreshold = 20971520)
public class FileUploadController {

	@PostMapping("/hello")
	public String hello() {
		return "Hello World!";
	}

	@GetMapping("/hello2")
	public String hello2() {
		return "Hello World!";
	}


	private static final String UPLOAD_DIR = "fhir/upload/bpmn/";



	@PostMapping(value = "/upload-xml", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public ResponseEntity<String> fileUpload(@RequestParam("bpmnXml") String bpmnXml) {

		if (bpmnXml == null || !bpmnXml.trim().startsWith("<?xml"))
			return ResponseEntity.badRequest().body("Invalid BPMN XML");

		try{
			String fileName = "bpmn" + System.currentTimeMillis() + ".bpmn";
			Path filepath = Paths.get(UPLOAD_DIR, fileName);
			Files.createDirectories(filepath.getParent());
			Files.writeString(filepath, bpmnXml);
			return ResponseEntity.ok("File uploaded successfully");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("Could not upload file: " + e.getMessage());
		}
	}

	@GetMapping("/uploadform")
	public String uploadform() {
		BufferedReader reader;
		StringBuilder lines = new StringBuilder();
		try {
			reader = new BufferedReader(new FileReader("fhir/htmlForm/form.html"));
			String line = reader.readLine();
			while (line != null) {
				lines.append(line).append("\n");
				line = reader.readLine();
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines.toString();
	}

	@GetMapping("/listfiles")
	public String listfiles() {
		StringBuilder sb = new StringBuilder();
		try {
			Path path = Paths.get(UPLOAD_DIR);
			Files.list(path).forEach(filePath -> {
				sb.append("<a href=\"/fhir/upload/bpmn/").append(filePath.getFileName()).append("\">").append(filePath.getFileName()).append("</a><br>");
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Path filePath =  Paths.get(System.getProperty("user.dir"),UPLOAD_DIR);
		Path exePath = Paths.get(System.getProperty("user.dir"), "fhir", "bpmn_to_petrinet", "bpmn_to_petri");

		int exitCode = bpmnToPetriBuilder(exePath.toString(), filePath.toString());
		if(exitCode != 0)
			ResponseEntity.status(500).body("Could not convert file:");
		return sb.toString();
	}

	@GetMapping("/files/{filename}")
	public ResponseEntity<String> getBpmFile(@PathVariable String filename) throws IOException {
		Path file = Paths.get(UPLOAD_DIR, filename);
		if (!Files.exists(file))
			return ResponseEntity.notFound().build();
		Path filePath =  Paths.get(System.getProperty("user.dir"),file.toString());
		Path exePath = Paths.get(System.getProperty("user.dir"), "fhir", "bpmn_to_petrinet", "bpmn_to_petri");

		int exitCode = bpmnToPetriBuilder(exePath.toString(), filePath.toString());
		if(exitCode != 0)
			ResponseEntity.status(500).body("Could not convert file:");


		String content = Files.readString(file);
		return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(content);

	}

	private int bpmnToPetriBuilder(String exePath, String path) {
		ProcessBuilder pb = new ProcessBuilder(exePath, path);
		try {
			Process pro = pb.start();
			return pro.waitFor();

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return 1;
	}

}
