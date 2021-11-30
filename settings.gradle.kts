enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "cmptw"

include(":core")
include(":core-windows")
include(":win32-keyboard-hook")
include(":win32-window-minsize-fix")
include(":app")
