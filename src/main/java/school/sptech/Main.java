package school.sptech;

import com.fasterxml.jackson.databind.JsonNode;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

public class Main {

    public static void main(String[] args) {

        // ------------------------------------------ S3 ---------------------------------------------------

        // Nessa parte o codigo vai pegar os dados dos buckes
        // aqui vai ser expecificamente o gold pela regra de negocio
        S3Client s3Client = new S3Provider().getS3Client();
        String bucketName = "nome-do-bucket"; // ex: bucket-raw-bronze-01
        String key = "caminho/ate/o/arquivo/do/bucket"; // ex: 01-bronze/2026-04-06_10-30_ap01.json

        //------------------------------------------------ MAIN -------------------------------------------------------------------

        Models models = new Models();

        try {
            // nesse momento vai procurar o bucket e como o nosso arquivo é json tive que importar outra biblioteca para salvar o json que estava no bucket
            // como uma variavel normal no java

            JsonNode goldData = models.lerJsonS3(s3Client,bucketName,key);

            // Aqui após lido o arquivo, separo a parte do json que o java vai usar,
            // em poucas palavras descartamos o resto json que é desnecessario
            goldData = goldData.get("alert_sender_data");

            // nesse momento a variavel ja é basicamente um vetor, só que melhor,
            // então eu levei em consideração que o json ele apenas vai se reescrever por cima,
            // assim ele passa por todas as anomalias que a ETL já separou
            for (int i = 0; i < goldData.size(); i++) {
                // nesse momento ele vai pegar o macaddress porque eu penso que é a melhor maneira de pegar os dados
                // do cliente e enviar para o JIRA de um modo mais seguro
                String macAddress = models.selectMacAddressFromMetricId(goldData.get(i).get("metric_id").asInt());
                try {
                    // aqui ele ja vai mandar os dados tanto para o JIRA
                    models.sendAlertToJira(macAddress);
                    // tanto salvar no MYSQL que enviou o alerta
                    models.insertAlerta(
                            goldData.get(i).get("metric_id").asInt(),
                            goldData.get(i).get("priority").asInt(),
                            goldData.get(i).get("title").asText(),
                            goldData.get(i).get("description").asText()
                    );
                } catch (RuntimeException e) {
                    throw new RuntimeException(e);
                }

                // aqui em baixo eu tava fazendo de uma maneira que n era para ser feito mas to com dó de apagar, então pode apenas iguinora-lo

//                    Integer cpuPercent = goldData.get(i).get("cpu_percent").asInt();
//                    Integer ramPercent = goldData.get(i).get("ram_percent").asInt();
//                    Integer diskPercent = goldData.get(i).get("disk_percent").asInt();
//
//                    if (cpuPercent >= models.selectMetricaFromMacAddressAndEspecificacao(macAddress, "cpu_percent").getMaximo()) {
//                        try {
//                            models.insertAlerta(models.selectMetricaFromMacAddressAndEspecificacao(macAddress, "cpu_percent").getId(),0,"","");
//                            models.sendAlertToJira(macAddress);
//                        } catch (RuntimeException e) {
//                            throw new RuntimeException(e);
//                        }
//
//                    } else if (cpuPercent <= models.selectMetricaFromMacAddressAndEspecificacao(macAddress, "cpu_percent").getMinimo()) {
//                        try {
//                            models.insertAlerta(models.selectMetricaFromMacAddressAndEspecificacao(macAddress, "cpu_percent").getId(),0,"","");
//                            models.sendAlertToJira(macAddress);
//                        } catch (RuntimeException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }

            }
        } catch (Exception e) {
            // vai pegar qualquer excessao que ocorra, e aqui encerra o código
            throw new RuntimeException(e);
        }

    }
}
