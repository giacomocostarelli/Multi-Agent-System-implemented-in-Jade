/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
public class Contestant2 extends Agent {

    Quiz kb2;
    private int step = 0;

    @Override
    protected void setup() {
        prepareKB2();
        registerServiceYP2();

        addBehaviour(new Behaviour() {

            public void action() {
                String answer;
                MessageTemplate mt0 = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                ACLMessage msg0 = receive(mt0);
                if (msg0 != null) {
                    answer = searchForAnswers(msg0.getContent());
                    if (!(answer.equals("NO"))) {
                        System.out.println("[Antonio]: " + answer);
                        ACLMessage reply0 = msg0.createReply();
                        reply0.setPerformative(ACLMessage.AGREE);
                        reply0.setContent(answer);
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Contestant2.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        send(reply0);
                        step++;
                    } else {
                        System.out.println("[Antonio]: Non Conosco la risposta!");
                    }

                } else {
                    block();
                }
            }

            public boolean done() {
                return step == 3;
            }

            //END BEHAV
        });

        addBehaviour(new Behaviour() {
            boolean ended = false;

            public void action() {
                MessageTemplate mt0 = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
                ACLMessage msg0 = receive(mt0);

                if (msg0 != null) {
                    System.out.println("[Antonio]: Chiusura.");
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

        //END SETUP
    }

    public String searchForAnswers(String question) {
        String answer = "NO";
        String[][] kb = kb2.getQuestionAnswerMatr();
        for (int i = 0; i < kb.length - 1; i++) {
            if (kb[i][0].equals(question)) {
                answer = kb[i][1];
            }
        }
        return answer;
    }

    public void prepareKB2() {
        try {
            kb2 = new Quiz(2);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Contestant2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void registerServiceYP2() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("answering-question");
        sd.setName("contestant-two");
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
