// package com.matheus.controle.ativos.controller;

// import java.io.IOException;
// import java.util.HashMap;
// import java.util.Map;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.multipart.MultipartFile;

// import com.matheus.controle.ativos.service.ExcelImportService;
// import com.matheus.controle.ativos.service.ExcelImportService.ImportResult;

// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.Parameter;
// import io.swagger.v3.oas.annotations.media.Content;
// import io.swagger.v3.oas.annotations.media.Schema;
// import io.swagger.v3.oas.annotations.responses.ApiResponse;
// import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import io.swagger.v3.oas.annotations.tags.Tag;

// @RestController
// @RequestMapping("/ativos/import")
// @Tag(name = "Importação Excel", description = "API para importação de ativos via planilha Excel")
// @CrossOrigin(origins = "*")
// public class ImportController {

//     @Autowired
//     private ExcelImportService excelImportService;

//     @PostMapping("/excel")
//     @Operation(summary = "Importar ativos de planilha Excel", description = "Faz upload de um arquivo .xlsx contendo dados de ativos para importação em lote. "
//             +
//             "A planilha deve conter as 49 colunas no formato padrão do Controle de Ativos.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "Importação concluída (pode conter erros parciais)", content = @Content(schema = @Schema(implementation = Map.class))),
//             @ApiResponse(responseCode = "400", description = "Arquivo inválido ou não é um Excel (.xlsx)"),
//             @ApiResponse(responseCode = "500", description = "Erro interno ao processar o arquivo")
//     })
//     public ResponseEntity<Map<String, Object>> importarExcel(
//             @Parameter(description = "Arquivo Excel (.xlsx) com os dados dos ativos", required = true) @RequestParam("file") MultipartFile file) {

//         Map<String, Object> response = new HashMap<>();

//         // Validação do arquivo
//         if (file.isEmpty()) {
//             response.put("sucesso", false);
//             response.put("mensagem", "Arquivo não pode estar vazio");
//             return ResponseEntity.badRequest().body(response);
//         }

//         if (!excelImportService.isExcelFile(file)) {
//             response.put("sucesso", false);
//             response.put("mensagem", "O arquivo deve ser um Excel (.xlsx)");
//             return ResponseEntity.badRequest().body(response);
//         }

//         try {
//             ImportResult result = excelImportService.importarAtivos(file);

//             response.put("sucesso", true);
//             response.put("mensagem", "Importação concluída");
//             response.put("importados", result.getImportados());
//             response.put("totalErros", result.getTotalErros());

//             if (result.hasErros()) {
//                 response.put("erros", result.getErros());
//             }

//             return ResponseEntity.ok(response);

//         } catch (IllegalArgumentException e) {
//             response.put("sucesso", false);
//             response.put("mensagem", e.getMessage());
//             return ResponseEntity.badRequest().body(response);

//         } catch (IOException e) {
//             response.put("sucesso", false);
//             response.put("mensagem", "Erro ao ler o arquivo: " + e.getMessage());
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

//         } catch (Exception e) {
//             response.put("sucesso", false);
//             response.put("mensagem", "Erro interno: " + e.getMessage());
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//         }
//     }
// }
