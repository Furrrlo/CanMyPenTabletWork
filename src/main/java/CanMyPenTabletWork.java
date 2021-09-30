import me.ferlo.cmptw.hook.KeyboardHookService;
import me.ferlo.cmptw.hook.KeyboardHookServiceImpl;
import me.ferlo.cmptw.window.WindowService;

public class CanMyPenTabletWork {
    public static void main(String[] args) throws Exception {

        final KeyboardHookService service = new KeyboardHookServiceImpl();
        service.addListener(event -> {
            System.out.println(event);
            return true;
        });

        WindowService.INSTANCE.register();
        service.register();

        System.out.println("Started");
        while(true) {
            Thread.sleep(100000);
        }
    }
}
