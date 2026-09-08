package co.wethinkcode.logisticsconnect;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import co.wethinkcode.logisticsconnect.TransitServiceApp.EtaResponse;
import co.wethinkcode.logisticsconnect.TransitServiceApp.StageResponse;
import co.wethinkcode.logisticsconnect.mq.MqConfig;
import io.javalin.Javalin;

public class TransitServiceApp {
    private static int latestStage = 0;

    public static Javalin createApp() {

        Javalin app = Javalin.create();

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/stage", ctx -> {
            ctx.json(new StageResponse(latestStage));
        });

        app.get("/eta/{hubId}", ctx -> {

            String hubId = ctx.pathParam("hubId");

            int etaMinutes = 30 + (latestStage * 10);

            ctx.json(new EtaResponse(
                    hubId,
                    latestStage,
                    etaMinutes
            ));
        });
        return app;
        // TODO (Calculates estimated arrival windows based on hub and delay stage.)
        // Add domain endpoints for transit-service here.
 
    }
    private static void startSubscriber() {
        Thread subscriberThread = new Thread(() -> {

            ActiveMQConnectionFactory connectionFactory =
                    new ActiveMQConnectionFactory(
                            MqConfig.BROKER_URL
                    );

            try {
                Connection connection =
                        connectionFactory.createConnection();

                Session session = connection.createSession(
                        false,
                        Session.AUTO_ACKNOWLEDGE
                );

                Topic topic =
                        session.createTopic(MqConfig.TOPIC);

                MessageConsumer consumer =
                        session.createConsumer(topic);

                consumer.setMessageListener(message -> {

                    try {
                        if (message instanceof TextMessage) {

                            String messageBody =
                                    ((TextMessage) message).getText();

                            System.out.println(
                                    "Received message: "
                                            + messageBody
                            );

                            String stageValue =
                                    messageBody
                                            .replace("{\"stage\":", "")
                                            .replace("}", "");

                            latestStage =
                                    Integer.parseInt(stageValue);

                            System.out.println(
                                    "Transit service updated stage to: "
                                            + latestStage
                            );
                        }

                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                });

                connection.start();

                System.out.println(
                        "Transit service subscribed to: "
                                + MqConfig.TOPIC
                );

            } catch (Exception exception) {
                exception.printStackTrace();
            }

        });

        subscriberThread.setDaemon(true);
        subscriberThread.start();
    }
    //Used by automated tests to simulate receiving an MQ message
    static void updateStageFromMessage(String messageBody) {

        String stageValue =
                messageBody
                        .replace("{\"stage\":", "")
                        .replace("}", "");

        latestStage = Integer.parseInt(stageValue);
    }
    static void resetStage() {
        latestStage = 0;
    }

    public static class StageResponse {

        private final int stage;

        public StageResponse(int stage) {
            this.stage = stage;
        }

        public int getStage() {
            return stage;
        }
    
    }
    public static class EtaResponse {

        private final String hubId;
        private final int stage;
        private final int etaMinutes;

        public EtaResponse(String hubId, int stage, int etaMinutes) {
            this.hubId = hubId;
            this.stage = stage;
            this.etaMinutes = etaMinutes;
        }

        public String getHubId() {
            return hubId;
        }

        public int getStage() {
            return stage;
        }

        public int getEtaMinutes() {
            return etaMinutes;
        }
    }
}

