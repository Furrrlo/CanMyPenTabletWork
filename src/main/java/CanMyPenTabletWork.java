import me.ferlo.cmptw.global.GlobalKeyboardHookService;
import me.ferlo.cmptw.hook.KeyboardHookEvent;
import me.ferlo.cmptw.hook.KeyboardHookListener;
import me.ferlo.cmptw.hook.KeyboardHookService;
import me.ferlo.cmptw.hook.KeyboardHookServiceImpl;
import me.ferlo.cmptw.raw.RawKeyboardInputService;
import me.ferlo.cmptw.window.WindowService;

public class CanMyPenTabletWork {
    public static void main(String[] args) throws Exception {

        WindowService.INSTANCE.register();
        RawKeyboardInputService.INSTANCE.register();
        GlobalKeyboardHookService.INSTANCE.register();

//        final KeyboardHookService service = new KeyboardHookServiceImpl();
//        service.addListener(new KeyboardHookListener() {
//            @Override
//            public void keyPressed(KeyboardHookEvent event) {
//                event.setCancelled(true);
//                System.out.println(event);
//            }
//        });
//        service.register();

        System.out.println("Started");
        while(true) {
            Thread.sleep(100000);
        }
    }
}
