public class ScannerStations {
    static class Station extends Thread {
        private final int scripts;
        Station(String name, int scripts) { super(name); this.scripts = scripts; }
        public void run() {
            try { Thread.sleep(scripts * 100L); System.out.println(getName() + " scanned " + scripts + " scripts"); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
    static class Worker implements Runnable {
        private final String name; private final int scripts;
        Worker(String name, int scripts) { this.name = name; this.scripts = scripts; }
        public void run() {
            try { Thread.sleep(scripts * 100L); System.out.println(name + " scanned " + scripts + " scripts"); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
    public static void main(String[] args) throws InterruptedException {
        Thread[] stations = { new Station("Station-1", 1), new Thread(new Worker("Station-2", 2)),
            new Station("Station-3", 3), new Thread(new Worker("Station-4", 4)) };
        for (Thread t : stations) t.start();
        System.out.println("Station-1 alive after start: " + stations[0].isAlive());
        for (Thread t : stations) t.join();
        System.out.println("All stations completed. Total scripts: 10");
    }
}
