package dev.tyoudm.assasin.core;

import dev.tyoudm.assasin.AssasinPlugin;
import dev.tyoudm.assasin.check.CheckManager;
import dev.tyoudm.assasin.data.DataManager;
import dev.tyoudm.assasin.mitigation.MitigationEngine;

public class ServiceContainer {
    private final AssasinPlugin plugin;
    
    private DataManager dataManager;
    private CheckManager checkManager;
    private MitigationEngine mitigationEngine;

    public ServiceContainer(AssasinPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        // 1. Iniciar Mitigación (El que banea/setback)
        this.mitigationEngine = new MitigationEngine();

        // 2. Iniciar Datos (El que guarda info de jugadores)
        this.dataManager = new DataManager();

        // 3. Iniciar Checks (El que procesa las trampas)
        this.checkManager = new CheckManager(mitigationEngine);
        
        plugin.getLogger().info("§aAll services wired correctly.");
    }

    public void disable() {
        // Limpieza de datos si es necesario
    }

    // Getters para que AssasinPlugin pueda acceder a ellos
    public DataManager getDataManager() { return dataManager; }
    public CheckManager getCheckManager() { return checkManager; }
    public MitigationEngine getMitigationEngine() { return mitigationEngine; }
}