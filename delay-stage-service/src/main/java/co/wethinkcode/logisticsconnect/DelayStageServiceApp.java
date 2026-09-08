package co.wethinkcode.logisticsconnect;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import co.wethinkcode.logisticsconnect.mq.MqConfig;
import io.javalin.Javalin;

public class DelayStageServiceApp {
    private static int currentStage = 0;

    public static Javalin createApp() {

        Javalin app = Javalin.create();

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/stage", ctx -> {
            ctx.json(new StageResponse(currentStage));
        });

        app.put("/stage/{stage}", ctx -> {

            int newStage = Integer.parseInt(
                    ctx.pathParam("stage")
            );

            if (newStage < 0 || newStage > 8) {
                ctx.status(400);
                ctx.result("Stage must be between 0 and 8");
                return;
            }

            currentStage = newStage;

            publishStageChange(newStage);

            ctx.json(new StageResponse(newStage));
        });
        return app;
// MQ TODO: publishes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.logisticsconnect.mq.MqConfig)

    }
    public static void main(String[] args) {
        createApp().start(7052);
    }
    private static void publishStageChange(int stage) {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(MqConfig.BROKER_URL);
        try ( 
            Connection connection = connectionFactory.createConnection()
        ){
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(MqConfig.TOPIC);
            MessageProducer producer = session.createProducer(topic);

            String messageBody =
                    "{\"stage\":" + stage + "}";

            TextMessage message =
                    session.createTextMessage(messageBody);

            producer.send(message);

            System.out.println(
                    "Published stage change: " + messageBody
            );

            producer.close();
            session.close();
        } catch (Exception exception) {
            System.err.println("Failed to publish stage change: " + exception.getMessage());
        }
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
}

