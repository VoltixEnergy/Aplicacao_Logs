package school.sptech;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Metrica {
    private Integer id;
    private Integer instanciaId;
    private Integer especificacaoId;
    private Double maximo;
    private Double minimo;
    private String unidadeMedida;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private LocalDateTime deletadoEm;

    public Integer getId() {
        return id;
    }

    public Integer getInstanciaId() {
        return instanciaId;
    }

    public Integer getEspecificacaoId() {
        return especificacaoId;
    }

    public Double getMaximo() {
        return maximo;
    }

    public Double getMinimo() {
        return minimo;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public LocalDateTime getDeletadoEm() {
        return deletadoEm;
    }
}
