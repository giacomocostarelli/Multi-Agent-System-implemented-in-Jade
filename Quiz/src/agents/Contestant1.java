package agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import quiz.Quiz;

/**
 * @author Giacomo
 */
public class Contestant1 extends Agent {

    Quiz kb1;
    private int step = 0;

    @Override
    protected void setup() {
        prepareKB1();
        registerServiceYP1();

        addBehaviour(new Behaviour() {
            private int step = 0;

            public void action() {
                String answer;
                MessageTemplate mt0 = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                ACLMessage msg0 = receive(mt0);
                if (msg0 != null) {
                    answer = searchForAnswers(msg0.getContent());
                    if (!(answer.equals("NO"))) {
                        System.out.println("[Marco]: " + answer);
                        ACLMessage reply0 = msg0.createReply();
                        reply0.setPerformative(ACLMessage.AGREE);
                        reply0.setContent(answer);
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Contestant1.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        send(reply0);
                        step++;
                    } else {
                        System.out.println("[Marco]: Non Conosco la risposta!");
                    }

                } else {
                    block();
                }
            }

            public boolean done() {
                return step == 3;
            }

        });

        addBehaviour(new Behaviour() {
            boolean ended = false;

            public void action() {
                MessageTemplate mt0 = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
                ACLMessage msg0 = receive(mt0);

                if (msg0 != null) {
                    System.out.println("[Marco]: Chiusura.");
                    ended = true;
                    this.myAgent.doDelete();
                } else {
                    block();
                }
            }

            public boolean done() {
                return ended;
            }
        });
    }

    public void prepareKB1() {
        try {
            kb1 = new Quiz(1);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Contestant2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String searchForAnswers(String question) {
        String answer = "NO";
        String[][] kb = kb1.getQuestionAnswerMatr();
        for (int i = 0; i < kb.length; i++) {
            if (kb[i][0].equals(question)) {
                answer = kb[i][1];
            }
        }
        return answer;
    }

    public void registerServiceYP1() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("answering-question");
        sd.setName("contestant-one");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException ex) {
            Logger.getLogger(Quizmaster.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
            System.exit(0);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

}
