package co.wethinkcode.logisticsconnect;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import co.wethinkcode.logisticsconnect.mq.MqConfig;
import io.javalin.Javalin;

public class AlertBotApp {
    private static final int ALERT_THRESHOLD = 5;

    public static void main(String[] args) {
        startSubscriber();
        Javalin app = Javalin.create().start(7054);

        app.get("/health", ctx -> ctx.result("OK"));

        // TODO (Posts proactive delay notifications to public transit social media pages (simulated).)
        // Mechanism: Outbound webhook, simulated social post
    }
    private static void startSubscriber() {

        Thread subscriberThread = new Thread(() -> {

            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(MqConfig.BROKER_URL);

            try {
                Connection connection = connectionFactory.createConnection();

                Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);

                Topic topic = session.createTopic(MqConfig.TOPIC);

                MessageConsumer consumer =
                        session.createConsumer(topic);

                consumer.setMessageListener(message -> {

                    try {
                        if (message instanceof TextMessage) {

                            String messageBody = ((TextMessage) message).getText();

                            System.out.println("AlertBot received: " + messageBody);

                            int stage = Integer.parseInt(messageBody.replace("{\"stage\":", "").replace("}", "")
                            );

                            if (stage >= ALERT_THRESHOLD) {
                                postAlert(stage);
                            } else {
                                System.out.println(
                                        "No alert needed. Stage "
                                                + stage
                                                + " is below threshold "
                                                + ALERT_THRESHOLD
                                );
                            }
                        }

                    } catch (Exception exception) {exception.printStackTrace();
                    }
                });

                connection.start();

                System.out.println(
                        "AlertBot subscribed to: "
                                + MqConfig.TOPIC
                );

            } catch (Exception exception) {exception.printStackTrace();
            }

        });

        subscriberThread.setDaemon(true);
        subscriberThread.start();
    }
    // Extracted so that the alert logic can be tested independently.
    public static boolean shouldAlert(int stage) {
        return stage >= ALERT_THRESHOLD;
    }

    // Extracted so that message processing can be tested independently.
    public static void processStageMessage(String messageBody) {
        int stage = Integer.parseInt(
                messageBody
                        .replace("{\"stage\":", "")
                        .replace("}", "")
        );

        if (shouldAlert(stage)) {
            postAlert(stage);
        } else {
            System.out.println(
                    "No alert needed. Stage "
                            + stage
                            + " is below threshold "
                            + ALERT_THRESHOLD
            );
        }
    }

    private static void postAlert(int stage) {
        System.out.println();
        System.out.println("================================");
        System.out.println("🚨 ALERT POSTED");
        System.out.println(
                "Transit delay has reached stage " + stage
        );
        System.out.println(
                "Threshold: " + ALERT_THRESHOLD
        );
        System.out.println("================================");
        System.out.println();
    }
}
