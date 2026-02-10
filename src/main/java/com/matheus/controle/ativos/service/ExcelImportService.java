// package com.matheus.controle.ativos.service;

// import java.io.IOException;
// import java.io.InputStream;
// import java.util.ArrayList;
// import java.util.Iterator;
// import java.util.List;

// import org.apache.poi.ss.usermodel.Cell;
// import org.apache.poi.ss.usermodel.CellType;
// import org.apache.poi.ss.usermodel.Row;
// import org.apache.poi.ss.usermodel.Sheet;
// import org.apache.poi.ss.usermodel.Workbook;
// import org.apache.poi.xssf.usermodel.XSSFWorkbook;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import org.springframework.web.multipart.MultipartFile;

// import com.matheus.controle.ativos.model.Ativo;
// import com.matheus.controle.ativos.model.enums.Status;
// import com.matheus.controle.ativos.repository.AtivoRepository;

// @Service
// public class ExcelImportService {

//     @Autowired
//     private AtivoRepository ativoRepository;

//     private static final String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    
//      // Valida se o arquivo é um Excel (.xlsx)

//     public boolean isExcelFile(MultipartFile file) {
//         return TYPE.equals(file.getContentType()) ||
//                 (file.getOriginalFilename() != null && file.getOriginalFilename().endsWith(".xlsx"));
//     }

//     /**
//      * Importa ativos de um arquivo Excel
//      * 
//      * @param file arquivo xlsx
//      * @return resultado da importação com contagem de sucessos e erros
//      */
//     public ImportResult importarAtivos(MultipartFile file) throws IOException {
//         if (!isExcelFile(file)) {
//             throw new IllegalArgumentException("O arquivo deve ser um Excel (.xlsx)");
//         }

//         List<Ativo> ativos = new ArrayList<>();
//         List<String> erros = new ArrayList<>();
//         int linhaAtual = 1; 

//         try (InputStream is = file.getInputStream();
//                 Workbook workbook = new XSSFWorkbook(is)) {

//             Sheet sheet = workbook.getSheetAt(0);
//             Iterator<Row> rows = sheet.iterator();

            
//             if (rows.hasNext()) {
//                 rows.next();
//             }

//             while (rows.hasNext()) {
//                 linhaAtual++;
//                 Row row = rows.next();

            
//                 if (isRowEmpty(row)) {
//                     continue;
//                 }

//                 try {
//                     Ativo ativo = parseRowToAtivo(row);

//                     // Valida campos obrigatórios
//                     if (ativo.getNomeAtivo() == null || ativo.getNomeAtivo().isBlank()) {
//                         erros.add("Linha " + linhaAtual + ": Nome do ativo é obrigatório");
//                         continue;
//                     }
//                     if (ativo.getPatrimonio() == null || ativo.getPatrimonio().isBlank()) {
//                         erros.add("Linha " + linhaAtual + ": Patrimônio é obrigatório");
//                         continue;
//                     }

//                     // Verifica se patrimônio já existe
//                     if (ativoRepository.findByPatrimonio(ativo.getPatrimonio()).isPresent()) {
//                         erros.add("Linha " + linhaAtual + ": Patrimônio '" + ativo.getPatrimonio() + "' já existe");
//                         continue;
//                     }

//                     ativos.add(ativo);
//                 } catch (Exception e) {
//                     erros.add("Linha " + linhaAtual + ": Erro ao processar - " + e.getMessage());
//                 }
//             }

//             // Salva todos os ativos válidos
//             List<Ativo> savedAtivos = ativoRepository.saveAll(ativos);

//             return new ImportResult(savedAtivos.size(), erros);

//         } catch (IOException e) {
//             throw new IOException("Erro ao ler o arquivo Excel: " + e.getMessage(), e);
//         }
//     }

    
//      //Converte uma linha do Excel em um objeto Ativo
     
//     private Ativo parseRowToAtivo(Row row) {
//         Ativo ativo = new Ativo();

//         // Coluna 0 (índice 0): Nome do Ativo
//         ativo.setNomeAtivo(getCellValueAsString(row.getCell(0)));

//         // Coluna 1: Domínio - comentado no model
//         // String dominio = getCellValueAsString(row.getCell(1));

//         // Coluna 2: Status
//         String statusStr = getCellValueAsString(row.getCell(2));
//         ativo.setStatus(parseStatus(statusStr));

//         // Coluna 3: Localidade
//         ativo.setLocalidade(getCellValueAsString(row.getCell(3)));

//         // Coluna 4: Setor
//         ativo.setSetor(getCellValueAsString(row.getCell(4)));

//         // Coluna 5: Responsável
//         ativo.setResponsavel(getCellValueAsString(row.getCell(5)));

//         // Coluna 6: Licença - comentado no model
//         // String licenca = getCellValueAsString(row.getCell(6));

//         // Coluna 7: Categoria - comentado no model
//         // String categoria = getCellValueAsString(row.getCell(7));

//         // Coluna 8: Marca/Fabricante - comentado no modelo
//         // String marcaFabricante = getCellValueAsString(row.getCell(8));

//         // Coluna 9: Modelo - comentado no model
//         // String modelo = getCellValueAsString(row.getCell(9));

//         // Coluna 10: Serial Number - comentado no model
//         // String serialNumber = getCellValueAsString(row.getCell(10));

//         // Coluna 11: Patrimônio
//         ativo.setPatrimonio(getCellValueAsString(row.getCell(11)));

//         // Colunas: Comentadas - mapeamento preparado para caso forem descomentadass
        

//         // Coluna 12: Linha
//         // Coluna 13: IMEI
//         // Coluna 14: Mac Address Ethernet
//         // Coluna 15: Mac Address Wifi
//         // Coluna 16: IP fixado
//         // Coluna 17: Processador
//         // Coluna 18: Memória RAM instalada
//         // Coluna 19: Armazenamento
//         // Coluna 20: Câmera Integrada
//         // Coluna 21: Classificação da Informação
//         // Coluna 22: Confidencialidade
//         // Coluna 23: Integridade
//         // Coluna 24: Disponibilidade
//         // Coluna 25: Fornecedor
//         // Coluna 26: Data de Aquisição do Ativo
//         // Coluna 27: Garantia/Validade do Suporte
//         // Coluna 28: Contato do Suporte
//         // Coluna 29: Nota Fiscal de Compra
//         // Coluna 30: Registro de mudança
//         // Coluna 31: Data da Última Avaliação
//         // Coluna 32: Próxima Avaliação do Ativo
//         // Coluna 33: BitLocker
//         // Coluna 34: Antivírus - Licença
//         // Coluna 35: Antivírus - Versão
//         // Coluna 36: BART / Wazuh
//         // Coluna 37: Chrome Enterprise
//         // Coluna 38: Acesso Remoto - ID
//         // Coluna 39: Acesso Remoto - Senha
//         // Coluna 40: Termo de Custódia
//         // Coluna 41: Senha Admin
//         // Coluna 42: Bloqueio de Tela
//         // Coluna 43: Conta Atribuída
//         // Coluna 44: Senha da Conta atribuída
//         // Coluna 45: Contrato, procedimentos e correlatos
//         // Coluna 46: Itens de configuração do Ativo
//         // Coluna 47: Manutenção Preventiva
//         // Coluna 48: OBS

//         return ativo;
//     }

    
//      //Converte o valor de uma célula para String
     
//     private String getCellValueAsString(Cell cell) {
//         if (cell == null) {
//             return null;
//         }

//         switch (cell.getCellType()) {
//             case STRING:
//                 String value = cell.getStringCellValue();
//                 return (value != null && !value.isBlank()) ? value.trim() : null;
//             case NUMERIC:
                
//                 double numValue = cell.getNumericCellValue();
//                 if (numValue == Math.floor(numValue)) {
//                     return String.valueOf((long) numValue);
//                 }
//                 return String.valueOf(numValue);
//             case BOOLEAN:
//                 return String.valueOf(cell.getBooleanCellValue());
//             case FORMULA:
//                 try {
//                     return cell.getStringCellValue();
//                 } catch (Exception e) {
//                     return String.valueOf(cell.getNumericCellValue());
//                 }
//             case BLANK:
//             default:
//                 return null;
//         }
//     }

    
//      //Converte string de status para o enum Status
    
//     private Status parseStatus(String statusStr) {
//         if (statusStr == null || statusStr.isBlank()) {
//             return Status.ESTOQUE; 
//         }

//         String normalized = statusStr.toUpperCase()
//                 .replace(" ", "_")
//                 .replace("-", "_")
//                 .replace("Ã", "A")
//                 .replace("Ç", "C");

//         try {
//             return Status.valueOf(normalized);
//         } catch (IllegalArgumentException e) {
            
//             if (normalized.contains("OPERAC"))
//                 return Status.OPERACIONAL;
//             if (normalized.contains("ESTOQ"))
//                 return Status.ESTOQUE;
//             if (normalized.contains("RESERV"))
//                 return Status.RESERVADO;

//             // MANUTENCAO e EXTRAVIADO estão comentados no enum,
//             // mapeando para ESTOQUE como fallback

//             if (normalized.contains("MANUT") || normalized.contains("EXTRAV"))
//                 return Status.ESTOQUE;

//             return Status.ESTOQUE;
//         }
//     }

   
//      // Verifica se uma linha está vazia
     
//     private boolean isRowEmpty(Row row) {
//         if (row == null)
//             return true;

//         for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
//             Cell cell = row.getCell(c);
//             if (cell != null && cell.getCellType() != CellType.BLANK) {
//                 String value = getCellValueAsString(cell);
//                 if (value != null && !value.isBlank()) {
//                     return false;
//                 }
//             }
//         }
//         return true;
//     }


//     // Classe para retornar o resultado da importação
   
//     public static class ImportResult {
//         private final int importados;
//         private final List<String> erros;

//         public ImportResult(int importados, List<String> erros) {
//             this.importados = importados;
//             this.erros = erros;
//         }

//         public int getImportados() {
//             return importados;
//         }

//         public List<String> getErros() {
//             return erros;
//         }

//         public int getTotalErros() {
//             return erros.size();
//         }

//         public boolean hasErros() {
//             return !erros.isEmpty();
//         }
//     }
// }
