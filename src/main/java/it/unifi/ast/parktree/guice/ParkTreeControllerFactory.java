package it.unifi.ast.parktree.guice;

import it.unifi.ast.parktree.controller.ParkTreeController;
import it.unifi.ast.parktree.view.ParkTreeView;

public interface ParkTreeControllerFactory {

	ParkTreeController create(ParkTreeView view);

}
