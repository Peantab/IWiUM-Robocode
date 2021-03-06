package iwium;

import robocode.*;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.function.Consumer;

import static java.lang.Double.max;

public class QLearningRobot extends AdvancedRobot {
    private static double epsilon = 0.7;
    private static double learningRate = 0.4;
    private static double discount = 0.4;

    private static final double minimumEpsilon = 0.01; // 0.05?
    private static final double minimumLR = 0.01;

    private boolean onScannedRobotTriggered = false;
    private boolean onHitByBulletTriggered = false;
    private double rawEnemyDistance = 0;

    private final static String filename = "iwiumKnowledge.ser";
    private final Random random = new Random();
    private static Map<Environment, Map<Action, Double>> knowledge = null;
    private final ArrayList<HistoryEvent> history= new ArrayList<>();

//    TODO:
//     * wyuczenie modelu
//     * Analiza statystyczna wyników

    private void initKnowledge() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(getDataFile(filename)))){
            ((SerializableState) in.readObject()).initializeState();
            System.out.println("Wczytałem stan wiedzy i parametry z pliku.");
            System.out.println("Wczytane parametry: epsilon=" + epsilon + ", LR=" + learningRate + ", discount=" + discount);
        } catch (IOException | ClassNotFoundException e) {
            knowledge = new HashMap<>();
            System.out.println("Stworzyłem nowy stan wiedzy.");
        }
    }

    @Override
    public void run() {
        setBodyColor(Color.blue);
        setGunColor(Color.cyan);
        setRadarColor(Color.blue);

        if(knowledge == null){
            initKnowledge();
        }

        Environment environment = prepareEnvironment();
        Environment newEnvironment;
        double energyOnStart = getEnergy();
        double energyOnEnd;
        while (true){
            Action action = pickAction(environment);
            action.enqueue(this);

            execute(); // to NIE jest tak na prawdę blokujące!
            while (getDistanceRemaining() > 0 && getTurnRemaining() > 0) { // obejście powyższego, ale nie da się tak na zaplanowane wystrzały!
                execute();
            }
            newEnvironment = prepareEnvironment();
            energyOnEnd = getEnergy();
            updateKnowledge(action, environment, newEnvironment, energyOnEnd - energyOnStart);
            environment = newEnvironment;
            energyOnStart = energyOnEnd;
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        rawEnemyDistance = event.getDistance();
        onScannedRobotTriggered = true;
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        onHitByBulletTriggered = true;
    }

    private Environment prepareEnvironment() {
        return new Environment(bucketEnergy(), bucketWallDistance(), bucketEnemyDistance(), bucketEvents());
    }

    private EventHappened bucketEvents() {
        EventHappened eventHappened;
        if (onScannedRobotTriggered && onHitByBulletTriggered) {
            eventHappened = EventHappened.BOTH_TRIGGERED;
        } else if (onScannedRobotTriggered) {
            eventHappened = EventHappened.SCANNED_ROBOT_TRIGGERED;
        } else if (onHitByBulletTriggered) {
            eventHappened = EventHappened.HIT_BY_BULLET_TRIGGERED;
        } else {
            eventHappened = EventHappened.NOTHING_HAPPENED;
        }

        onScannedRobotTriggered = false;
        onHitByBulletTriggered = false;

        return eventHappened;
    }

    private EnergyStatus bucketEnergy() {
        EnergyStatus energyStatus;

        // Energy is a double between 0.0 and 100.0
        double energy = getEnergy();

        if (energy > 50) energyStatus = EnergyStatus.HIGH;
        else if (energy > 20) energyStatus = EnergyStatus.MODERATE;
        else energyStatus = EnergyStatus.LOW;

        return energyStatus;
    }

    private Distance bucketWallDistance() {
        double heading = getHeading();
        double x = getX();
        double y = getY();
        double maxX = getBattleFieldWidth(); // By default 800
        double maxY = getBattleFieldHeight(); // By default 600

        double distance;
        if (heading == 0) distance = maxY - y;
        else if (heading == 90) distance = maxX - x;
        else if (heading == 180) distance = y;
        else if (heading == 270) distance = x;
        else {
            double a = Math.tan(90 - heading);
            double b = y - a * x;
            double leftRight;
            double upDown;

            if (heading < 180){
                double yIntercept = a * maxX + b;
                leftRight = Math.sqrt((maxX - x) * (maxX - x) + (yIntercept - y) * (yIntercept - y));
            } else {
                leftRight = Math.sqrt((x * x) + (y - b) * (y - b));
            }

            if (heading < 90 || heading > 270) {
                double xIntercept = (maxY - b) / a;
                upDown = Math.sqrt((xIntercept - x) * (xIntercept - x) + (maxY - y) * (maxY - y));
            } else {
                double xIntercept = -b / a;
                upDown = Math.sqrt((xIntercept - x) * (xIntercept - x) + y*y);
            }
            distance = Math.min(leftRight, upDown);
        }

        Distance distanceToWall;
        if (distance < 40) distanceToWall = Distance.CLOSE;
        else if (distance < 550) distanceToWall = Distance.MEDIUM;
        else distanceToWall = Distance.FAR;

        return distanceToWall;
    }

    private Distance bucketEnemyDistance() {
        Distance distanceToEnemy;

        if (onScannedRobotTriggered) {
            if (rawEnemyDistance < 50) distanceToEnemy = Distance.CLOSE;
            else if (rawEnemyDistance < 200) distanceToEnemy = Distance.MEDIUM;
            else distanceToEnemy = Distance.FAR;
        } else {
            distanceToEnemy = Distance.FAR;
        }

        return distanceToEnemy;
    }

    private Action pickAction(Environment environment) {
        Action action;

        if (random.nextDouble() > epsilon && knowledge.containsKey(environment)) {
            Map<Action, Double> rewardsForActions = knowledge.get(environment);
            action = rewardsForActions.entrySet().stream().max(Comparator.comparingDouble(Map.Entry::getValue)).get().getKey();
            out.println("Wybrałem akcję " + action);
        } else {
            Action[] allValues = Action.values();
            action = allValues[random.nextInt(allValues.length)];
            out.println("Wylosowałem akcję " + action);
        }

        return action;
    }

    private void updateKnowledge(Action action, Environment observation, Environment newObservation, Double reward) {
        out.println("W stanie " + observation + " wykonałem " + action + ", co poskutkowało nagrodą " + reward
                + " i stanem " + newObservation + ".");
        double oldValue = knowledge.getOrDefault(observation, Collections.emptyMap()).getOrDefault(action, 0.0);

        double maxFutureReward = knowledge.getOrDefault(newObservation, Collections.emptyMap()).values().stream()
                .max(Double::compareTo).orElse(0.0);

        double newValue = (1 - learningRate) * oldValue + learningRate * (reward + discount * maxFutureReward);

        knowledge.computeIfAbsent(observation, environment -> new HashMap<>()).put(action, newValue);
        updateHistory(observation, action);
        normalize();
    }

    private void updateHistory(Environment newObservation, Action newAction) {
        HistoryEvent newHistoryEvent = new HistoryEvent(newObservation, newAction);
        history.add(newHistoryEvent);
        if(history.size() >= 35) {
            for (HistoryEvent event: history.subList(0, 25)) {
                Environment observation = event.getObservation();
                Action action = event.getAction();
                knowledge.get(observation).compute(action, (a, v) -> v += 0.1);
            }
            history.remove(0);
        }
    }

    private void normalize() {
        double max = knowledge.entrySet().stream().flatMap((e) -> e.getValue().values().stream()).map(Math::abs).max(Comparator.naturalOrder()).orElse(1.0);
        if (max > 1) {
            for (Map.Entry<Environment, Map<Action, Double>> knowledgeAsEntries: knowledge.entrySet()) {
                Map<Action, Double> rewardsForActions = knowledgeAsEntries.getValue();
                rewardsForActions.replaceAll((a, v) -> rewardsForActions.get(a) / max);
            }
        }
    }

    @Override
    public void onDeath(DeathEvent event) {
        super.onDeath(event);
        try (ObjectOutputStream objectOut = new ObjectOutputStream(new RobocodeFileOutputStream(getDataFile(filename)))) {
            objectOut.writeObject(new SerializableState(knowledge, max(0.99 * epsilon, minimumEpsilon), max(0.99 * learningRate, minimumLR), discount));
            out.println("Zapisałem stan");
        } catch (IOException e) {
            out.println("Błąd podczas zapisywania stanu.");
            e.printStackTrace();
        }
    }

    private static class Environment implements Serializable {
        private final EnergyStatus energyStatus;
        private final Distance distanceToWall;
        private final Distance distanceToEnemy;
        private final EventHappened eventHappened;

        private Environment(EnergyStatus energyStatus, Distance distanceToWall, Distance distanceToEnemy, EventHappened eventHappened) {
            this.energyStatus = energyStatus;
            this.distanceToWall = distanceToWall;
            this.distanceToEnemy = distanceToEnemy;
            this.eventHappened = eventHappened;
        }

        @Override
        public String toString() {
            return "Environment{" +
                    "energyStatus=" + energyStatus +
                    ", distanceToWall=" + distanceToWall +
                    ", distanceToEnemy=" + distanceToEnemy +
                    ", eventHappened=" + eventHappened +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Environment that = (Environment) o;
            return eventHappened == that.eventHappened &&
                    energyStatus == that.energyStatus &&
                    distanceToWall == that.distanceToWall &&
                    distanceToEnemy == that.distanceToEnemy;
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventHappened, energyStatus, distanceToWall, distanceToEnemy);
        }
    }

    private enum EventHappened {
        SCANNED_ROBOT_TRIGGERED, HIT_BY_BULLET_TRIGGERED, BOTH_TRIGGERED, NOTHING_HAPPENED
    }

    private enum EnergyStatus {
        LOW, MODERATE, HIGH
    }

    private enum Distance {
        CLOSE, MEDIUM, FAR
    }

    private enum Action {
        DO_NOTHING((AdvancedRobot a) -> {}),
        GO_FORWARD((AdvancedRobot a) -> a.setAhead(25)),
        GO_BACKWARD((AdvancedRobot a) -> a.setAhead(25)),
        TURN_LEFT((AdvancedRobot a) -> a.setTurnLeft(30)),
        TURN_RIGHT((AdvancedRobot a) -> a.setTurnRight(30)),
        FIRE_LIGHT((AdvancedRobot a) -> {a.setFire(0.5); a.setAhead(8);}),
        FIRE_HARD((AdvancedRobot a) -> {a.setFire(2.5); a.setAhead(10);});

        Consumer<AdvancedRobot> action;

        Action(Consumer<AdvancedRobot> action){
            this.action = action;
        }

        void enqueue(AdvancedRobot r) {
            action.accept(r);
        }
    }

    private static class HistoryEvent {

        private final Action action;
        private final Environment observation;

        public HistoryEvent(Environment observation, Action action) {
            this.observation = observation;
            this.action = action;
        }

        public Environment getObservation() {
            return observation;
        }

        public Action getAction() {
            return action;
        }
    }

    private static class SerializableState implements Serializable {
        private final Map<Environment, Map<Action, Double>> environment;
        private final double epsilon;
        private final double learningRate;
        private final double discount;

        public SerializableState(Map<Environment, Map<Action, Double>> environment, double epsilon, double learningRate, double discount) {
            this.environment = environment;
            this.epsilon = epsilon;
            this.learningRate = learningRate;
            this.discount = discount;
        }

        void initializeState() {
            QLearningRobot.knowledge = environment;
            QLearningRobot.epsilon = epsilon;
            QLearningRobot.learningRate = learningRate;
            QLearningRobot.discount = discount;
        }
    }
}
