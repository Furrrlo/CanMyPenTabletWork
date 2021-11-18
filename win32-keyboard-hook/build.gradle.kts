plugins {
    `cpp-library`
}

library {
    targetMachines.set(listOf(machines.windows.x86, machines.windows.x86_64))

    binaries.whenElementFinalized {
        val binary = this
        val binaryToolChain = binary.toolChain
        if(binaryToolChain !is VisualCpp)
            return@whenElementFinalized

        compileTask.get().apply {
            macros["_WINDOWS"] = null
            macros["_USRDLL"] = null
            if(binary.targetMachine.architecture.name == MachineArchitecture.X86)
                macros["WIN32"] = null

            if(binary.isDebuggable)
                macros["_DEBUG"] = null
            else
                macros["NDEBUG"] = null

            macros["UNICODE"] = null
            macros["_UNICODE"] = null

            macros["NEHKBDHOOK_EXPORTS"] = null
        }

        if(this is CppSharedLibrary)
            linkTask.get().linkerArgs.addAll("user32.lib")
    }
}