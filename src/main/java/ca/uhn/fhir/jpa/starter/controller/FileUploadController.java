package ca.uhn.fhir.jpa.starter.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


import javax.servlet.annotation.MultipartConfig;
import javax.xml.parsers.DocumentBuilderFactory;

@RestController
@RequestMapping("/bpmn")
@MultipartConfig(fileSizeThreshold = 20971520)
public class FileUploadController {

	// basic test endpoint
	@PostMapping("/hello")
	public String hello() {
		return "Hello World!";
	}

	@GetMapping("/hello2")
	public String hello2() {
		return "Hello World!";
	}


	private static final String UPLOAD_DIR = "fhir/upload/bpmn/";
	private static final String PNML_DIR = "fhir/upload/pnml/";

	// convert bpmn to xml and generates pnml file
	@PostMapping("/convert-bpmn")
	public ResponseEntity<String> uploadAndConvertXml(@RequestBody Map<String, String> payload) throws IOException {
		String xml = payload.get("xml");
		String filename = payload.get("filename");
		if (xml == null || filename == null || filename.isBlank()) {
			return ResponseEntity.badRequest().body("missing content");
		}

		Path uploadDir = Paths.get(System.getProperty("user.dir"), "fhir","upload", "bpmn");
		Files.createDirectories(uploadDir);
		Path bpmnFile = uploadDir.resolve(filename + ".bpmn");
		Files.writeString(bpmnFile, xml);

		Path exePath = Paths.get(System.getProperty("user.dir"), "fhir", "bpmn_to_petrinet", "bpmn_to_petri.py");
		int exitCode = bpmnToPetriBuilder(exePath.toString(), bpmnFile.toString());
		if (exitCode != 0) {
			return ResponseEntity.status(500).body("converting error");
		}

		Path pnmlFile = Paths.get(System.getProperty("user.dir"), "fhir","upload", "pnml", filename + ".pnml");
		if (!Files.exists(pnmlFile)) {
			return ResponseEntity.status(500).body("PNML-File not found");
		}

		String pnmlContent = Files.readString(pnmlFile);
		return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(pnmlContent);
	}


	// // lists .bpmn files and triggers conversion for each
	@GetMapping("/files/bpmn")
	public String listFiles() {
		StringBuilder sb = new StringBuilder();
		try {
			Path path = Paths.get(UPLOAD_DIR);
			Files.list(path).forEach(filePath -> {
				sb.append("<a href=\"/bpmn/files/bpmn/")
					.append(filePath.getFileName())
					.append("\">")
					.append(filePath.getFileName())
					.append("</a><br>");
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

	 	// does not show files if one file is incorrect / but converts the other
		 Path filePath = Paths.get(System.getProperty("user.dir"), UPLOAD_DIR);
		 Path exePath = Paths.get(System.getProperty("user.dir"), "fhir", "bpmn_to_petrinet", "bpmn_to_petri.py");

		 int exitCode = bpmnToPetriBuilder(exePath.toString(), filePath.toString());
		 if (exitCode != 0)
			return "Could not convert file";

		return sb.toString();
	}


	// lists all .pnml files in the upload directory (with HTML links)
	@GetMapping("/files/pnml")
	public String listPnmlFiles() {
		StringBuilder sb = new StringBuilder();
		try {
			Path path = Paths.get(PNML_DIR);
			Files.list(path).forEach(filePath -> {
				sb.append("<a href=\"/bpmn/files/pnml/")
					.append(filePath.getFileName())
					.append("\">")
					.append(filePath.getFileName())
					.append("</a><br>");
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}


	// returns the content of a .bpmn file as plain text (also triggers conversion)
	@GetMapping("/files/bpmn/{filename}")
	public ResponseEntity<String> getBpmFile(@PathVariable String filename) throws IOException {
		Path file = Paths.get(UPLOAD_DIR, filename);
		if (!Files.exists(file))
			return ResponseEntity.notFound().build();
		Path filePath =  Paths.get(System.getProperty("user.dir"),file.toString());
		Path exePath = Paths.get(System.getProperty("user.dir"), "fhir", "bpmn_to_petrinet", "bpmn_to_petri.py");

		int exitCode = bpmnToPetriBuilder(exePath.toString(), filePath.toString());
		if(exitCode != 0)
			ResponseEntity.status(500).body("Could not convert file:");


		String content = Files.readString(file);
		return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(content);

	}


	// returns a specific PNML file as raw XML
	@GetMapping("/files/pnml/{filename}")
	public ResponseEntity<String> getPnmlFile(@PathVariable String filename) throws IOException {
		Path pnmlFile = Paths.get(PNML_DIR, filename).normalize();
		if (!Files.exists(pnmlFile))
			return ResponseEntity.status(404).body("PNML file not found");

		String content = Files.readString(pnmlFile);
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(content);
	}


	//  parses PNML and returns Petri net structure as JSON
	@GetMapping(value = "/files/pnml/{filename}/petrinet", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getPnmlFilePetri(@PathVariable String filename) throws Exception {
		Path pnmlFile = Paths.get(PNML_DIR, filename).normalize();
		if (!Files.exists(pnmlFile))
			return ResponseEntity.status(404).body("PNML file not found");

		Document doc = DocumentBuilderFactory.newInstance()
			.newDocumentBuilder().parse(( Files.newInputStream(pnmlFile)));
		doc.getDocumentElement().normalize();

		NodeList places = doc.getElementsByTagName("place");
		NodeList transitions = doc.getElementsByTagName("transition");
		NodeList arcs = doc.getElementsByTagName("arc");

		Map<String, Object> net = new HashMap<>();
		net.put("places", extractIds(places));
		net.put("transitions", extractIds(transitions));
		net.put("arcs", extractArcs(arcs));

		return ResponseEntity.ok(net);
	}


	// form to upload XML to PNML
	@GetMapping("/forms/upload")
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


	// loads and displays the HTML view for the Petri net
	@GetMapping("/view/petrinet")
	@ResponseBody
	public String viewForms() {
		StringBuilder lines = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new FileReader("fhir/htmlForm/petrinet.html"))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "Error loading HTML";
		}

		return lines.toString();
	}


	// shows HTML form for BPMN to PNML conversion (xml to PNML)
	@GetMapping("/view")
	public String viewForm() {
		try (BufferedReader reader = new BufferedReader(new FileReader("fhir/htmlForm/bpmnToPnml.html"))) {
			return reader.lines().reduce("", (a, b) -> a + b + "\n");
		} catch (IOException e) {
			e.printStackTrace();
			return "Error: File not found";
		}
	}


	// shows HTML form for BPMN to PNML conversion (file to PNML)
	@GetMapping("/view2")
	public String viewForm2() {
		try (BufferedReader reader = new BufferedReader(new FileReader("fhir/htmlForm/bpmnToPnml_2.html"))) {
			return reader.lines().reduce("", (a, b) -> a + b + "\n");
		} catch (IOException e) {
			e.printStackTrace();
			return "Error: File not found";
		}
	}

 	// convert BPMN to PNML (via Python script)
	private int bpmnToPetriBuilder(String exePath, String path) {
		ProcessBuilder pb = new ProcessBuilder("python", exePath, path);
		pb.redirectErrorStream(true);
		try {
			Process pro = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
			return pro.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return 1;
	}
	// extractIds/Names for petrinet
	private List<Map<String, String>> extractIds(NodeList nodes) {
		List<Map<String, String>> list = new ArrayList<>();

		for (int i = 0; i < nodes.getLength(); i++) {
			Element element = (Element) nodes.item(i);
			String id = element.getAttribute("id");
			if (id == null || id.trim().isEmpty()) continue;

			String name = id;

			NodeList nameList = element.getElementsByTagName("name");
			if (nameList.getLength() > 0) {
				Element nameElement = (Element) nameList.item(0);
				NodeList textList = nameElement.getElementsByTagName("text");
				if (textList.getLength() > 0) {
					String text = textList.item(0).getTextContent().trim();
					if (!text.isEmpty()) {
						name = text;
					}
				}
			}

			Map<String, String> entry = new HashMap<>();
			entry.put("id", id);
			entry.put("name", name);
			list.add(entry);
		}

		return list;
	}

	// extract Arcs for petrinet
	private List<Map<String, String>> extractArcs(NodeList arcs) {
		List<Map<String, String>> arcList = new ArrayList<>();
		for (int i = 0; i < arcs.getLength(); i++) {
			Element el = (Element) arcs.item(i);
			Map<String, String> arc = new HashMap<>();
			arc.put("source", el.getAttribute("source"));
			arc.put("target", el.getAttribute("target"));
			arcList.add(arc);
		}
		return arcList;
	}
}
