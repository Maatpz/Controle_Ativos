package com.matheus.controle.ativos.controller;

@RestController
@RequestMapping("/ativos")
public class AtivoController {
    
    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<Ativo> createAtivo(@Valid @RequestBody Ativo ativo){
        try{
            Ativo novoAtivo = ativo.service.save(ativo);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoAtivo);
        }catch (Exception e) {
           retrun ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); 
        }

    }

    @GetMapping
    public ResponseEntity <List<Ativo>> getAllAtivos(){
        List<Ativo> ativos = ativoService.findALl();
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ativo> getAtivoById(@PathVariable UUID id){
        Optional <Ativo> ativo = ativoService.findById(id);
        return ativo.map(m -> ResponseEntity.ok(m))
        .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ativo> updateAtivo(@PathVariable UUID id, @Valid @RequestBody Ativo ativo) {
        try {
            Ativo ativoAtualizado = ativoService.updateAtivo(id, ativo);
            if (ativoAtualizado != null) {
                return ResponseEntity.ok(ativoAtualizado);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAtivo(@PathVariable UUID id) {
        try {
            if (ativoService.existsById(id)) {
                ativoService.deleteById(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    
}
