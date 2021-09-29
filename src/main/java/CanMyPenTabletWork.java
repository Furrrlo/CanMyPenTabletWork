import me.ferlo.cmptw.hook.KeyboardHookEvent;
import me.ferlo.cmptw.hook.KeyboardHookListener;
import me.ferlo.cmptw.hook.KeyboardHookService;
import me.ferlo.cmptw.hook.KeyboardHookServiceImpl;

public class CanMyPenTabletWork {
    public static void main(String[] args) throws Exception {
        final KeyboardHookService service = new KeyboardHookServiceImpl();
        service.addListener(new KeyboardHookListener() {
            @Override
            public void keyPressed(KeyboardHookEvent event) {
                event.setCancelled(true);
                System.out.println(event);
            }
        });
        service.register();
        System.out.println("Started");

        while(true) {
            Thread.sleep(100000);
        }
    }
}
