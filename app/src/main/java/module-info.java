module com.github.furrrlo.cmptw.app {
    requires com.github.furrrlo.cmptw.core;

    requires org.slf4j;
    requires jul.to.slf4j;

    requires net.harawata.appdirs;

    requires com.google.gson;
    exports com.github.furrrlo.cmptw.app.hook to com.google.gson;

    requires java.desktop;
    requires com.github.furrrlo.cmptw.windows.window_minsize_fix;
    requires darklaf.core;
    requires org.bidib.org.oxbow.swingbits;
    requires org.swinglabs.swingx.core;
    requires com.miglayout;
    requires com.github.jiconfont.core;
    requires com.github.jiconfont.swing;
    requires com.github.jiconfont.font_awesome;
    requires com.fifesoft.rsyntaxtextarea;
    requires com.fifesoft.autocomplete;
    requires darklaf.extensions.rsyntaxarea;
}