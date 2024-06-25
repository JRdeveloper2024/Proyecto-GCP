package com.example.proyect;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/archivos")
public class ArchivosController {

    private static final String UPLOAD_DIR = "./uploads"; // Directorio de subida de archivos

    @PostMapping("/subir")
    public ResponseEntity<String> manejarSubidaArchivo(@RequestParam("archivo") MultipartFile archivo) {
        try {
            // Generar un nombre único para el archivo
            String nombreArchivo = UUID.randomUUID().toString() + "_" + archivo.getOriginalFilename();

            // Guardar el archivo en el sistema de archivos
            Path rutaArchivo = Paths.get(UPLOAD_DIR).resolve(nombreArchivo);
            Files.copy(archivo.getInputStream(), rutaArchivo);

            // Construir la URL de descarga del archivo
            String uriDescargaArchivo = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/archivos/descargar/")
                    .path(nombreArchivo)
                    .toUriString();

            return ResponseEntity.ok().body("Archivo subido correctamente. URL de descarga: " + uriDescargaArchivo);
        } catch (IOException ex) {
            return ResponseEntity.status(500).body("Error al subir archivo.");
        }
    }

    @GetMapping("/descargar/{nombreArchivo:.+}")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable String nombreArchivo) {
        // Cargar archivo como recurso
        Path rutaArchivo = Paths.get(UPLOAD_DIR).resolve(nombreArchivo);
        Resource recurso;
        try {
            recurso = new UrlResource(rutaArchivo.toUri());
        } catch (MalformedURLException ex) {
            return ResponseEntity.notFound().build();
        }

        // Preparar la respuesta para descargar el archivo
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
                .body(recurso);
    }
}
