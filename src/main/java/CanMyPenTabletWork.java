import me.ferlo.cmptw.hook.KeyboardHookService;
import me.ferlo.cmptw.hook.KeyboardHookServiceImpl;
import me.ferlo.cmptw.window.WindowService;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.awt.event.KeyEvent;

public class CanMyPenTabletWork {
    public static void main(String[] args) throws Exception {
        // Redirect jul to slf4j
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        final KeyboardHookService service = new KeyboardHookServiceImpl();
        service.addListener(event -> {
            if(!event.isKeyDown())
                return false;

            final StringBuilder sb = new StringBuilder();
            sb.append(event.device().getId());
            sb.append(": ");
            if(event.modifiers() != 0)
                sb.append(event.getModifiersText(" + ")).append(" + ");
            sb.append(KeyEvent.getKeyText(event.awtKeyCode()));
            System.out.println(sb);
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
