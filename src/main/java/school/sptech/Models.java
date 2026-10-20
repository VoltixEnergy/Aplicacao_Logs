package school.sptech;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.core.BeanPropertyRowMapper;
import school.sptech.config.Jira;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
//import java.util.List;


public class Models {

// ----------------------------------- MYSQL -------------------------------------------------------------

    Dev conexao = new Dev();
    JdbcTemplate jdbcTemplate = new JdbcTemplate(conexao.getConexao());

    public void insertAlerta(Integer metricaId, Integer prioridade, String tipo, String descricao) {
        String sql = """
                INSERT INTO voltix.alerta (metrica_id, prioridade, tipo, descricao, enviado_em)
                	VALUES (?,?,?,?,?);
                """;

        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        System.out.println("Vou inserir no banco:");
        System.out.println(sql);
        System.out.println(metricaId);
        System.out.println(prioridade);
        System.out.println(tipo);
        System.out.println(descricao);
        System.out.println(dateTime.format(format));

        jdbcTemplate.update(sql,
                metricaId,
                prioridade,
                tipo,
                descricao,
                dateTime.format(format)
        );
    }

//    public List<Metrica> selectMetricaForMacAddress(String macAddress) {
//        String sql = """
//                SELECT metrica.* FROM voltix.metrica
//                JOIN voltix.instancia
//                ON metrica.instancia_id = instancia.id
//                WHERE instancia.endereco_mac = ?
//                """;
//
//        List<Metrica> metricasForMacAddress = jdbcTemplate.query(sql,new BeanPropertyRowMapper<>(Metrica.class),macAddress);
//        return metricasForMacAddress;
//    }

    public String selectEmailFromMacAddress(String macAddress) {
        String sql = """
                SELECT contato.email
                FROM voltix.contato
                JOIN voltix.empresa
                ON contato.empresa_id = empresa.id
                JOIN voltix.instancia
                ON empresa.id = instancia.empresa_id
                WHERE instancia.endereco_mac = ?
                LIMIT 1
                """;
        System.out.println("Vou fazer a query:");
        System.out.println(sql);
        String email = jdbcTemplate.queryForObject(sql,String.class,macAddress);
        return email.trim().toLowerCase();
    }

    public String selectUrlFromMacAddress(String macAddress) {
        String sql = """
                SELECT contato.url
                FROM voltix.contato
                JOIN voltix.empresa
                ON contato.empresa_id = empresa.id
                JOIN voltix.instancia
                ON empresa.id = instancia.empresa_id
                WHERE instancia.endereco_mac = ?
                LIMIT 1
                """;
        System.out.println("Vou fazer a query:");
        System.out.println(sql);
        String url = jdbcTemplate.queryForObject(sql,String.class,macAddress);
        return url.trim().toLowerCase();
    }

    public String selectMacAddressFromMetricId(Integer metricId) {
        String sql = """
                SELECT instancia.endereco_mac
                FROM voltix.instancia
                JOIN voltix.metrica
                ON  metrica.instancia_id = instancia.id
                WHERE metrica.id = ?
                LIMIT 1
                """;
        System.out.println("Vou fazer a query:");
        System.out.println(sql);
        String macAddres = jdbcTemplate.queryForObject(sql,String.class,metricId);
        return macAddres;
    }

//    public Metrica selectMetricaFromMacAddressAndEspecificacao(String macAddress,String especificacao) {
//        String sql = """
//                SELECT instancia.endereco_mac
//                FROM voltix.instancia
//                JOIN voltix.metrica
//                ON  metrica.instancia_id = instancia.id
//                JOIN voltix.especificacao
//                ON metrica.especificacao_id = especificacao.id
//                WHERE instancia.endereco_mac = ? AND especificacao.nome = ?
//                LIMIT 1;
//                """;
//        Metrica metrica = jdbcTemplate.queryForObject(sql,Metrica.class,macAddress,especificacao);
//        return metrica;
//    }

// ----------------------------------- AWS S3 -------------------------------------------------------------

    public JsonNode lerJsonS3(
            S3Client s3,
            String bucket,
            String chave
    ) throws Exception {
        System.out.println(bucket);
        System.out.println(chave);

        System.out.println("Vou pegar o arquivo");
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(chave)
                .build();
        System.out.println("Arquivo pego com sucesso!");
        System.out.println("Vou tentar le-lo e converte-lo");
        try (ResponseInputStream<GetObjectResponse> input =
                     s3.getObject(request)) {
            ObjectMapper mapper = new ObjectMapper();
            System.out.println("Convertido com sucesso!");
            return mapper.readTree(input);
        }
    }

    public void moverArquivoS3(
            S3Client s3,
            String bucketOrigem,
            String keyOrigem,
            String bucketDestino,
            String keyDestino
    ) {
        System.out.println(bucketOrigem);
        System.out.println(keyOrigem);
        System.out.println(bucketDestino);
        System.out.println(keyDestino);
        try {
            System.out.println("Vou copiar o arquivo para o destino colocado");
            s3.copyObject(CopyObjectRequest.builder()
                    .sourceBucket(bucketOrigem)
                    .sourceKey(keyOrigem)
                    .destinationBucket(bucketDestino)
                    .destinationKey(keyDestino)
                    .build()
            );
            System.out.println("Copiado!");
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        try {
            System.out.println("Vou excluir o arquivo do destino de origem");
            s3.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucketOrigem)
                            .key(keyOrigem)
                            .build()
            );
            System.out.println("Excluido e processo concluido com sucesso!");
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

// ----------------------------------- JIRA -------------------------------------------------------------

    public void sendAlertToJira(String macAddress, String title) {

        String baseUrl = selectUrlFromMacAddress(macAddress); // ex: https://seu-dominio.atlassian.net
        System.out.println("Dei select no url");
        String email = selectEmailFromMacAddress(macAddress); // ex: nome.sobrenome@email.com --> detalhe: ele tem que ser um membro no jira
        System.out.println("Dei select no email");
        // LEMBRA DE MUDAR O TOKEN DO JIRA, ELE TROCA DEPOIS DE UM TEMPO
        String apiToken = "API_TOKEN";
        Jira jira = new Jira(baseUrl, email, apiToken);
        System.out.println("Criei a conexão com o Jira");

        try {
            // Cria uma issue no backlog do projeto informado
            String response = jira.createIssue(
                    "KAN", // Key do projeto, presente na URL do seu site
                    title, // Nome da issue
                    "Task" // Tipo da issue: "Task", "Bug", "Story", etc.
            );

            // Exibe o JSON de resposta com os dados da issue criada
            System.out.println(response);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
