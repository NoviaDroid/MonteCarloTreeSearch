package itu.ejuuragr.highdk;

import itu.ejuuragr.MCTSTools;
import itu.ejuuragr.UCT.SimpleMCTS;

public class LimitedActions2 extends SimpleMCTS {

	public LimitedActions2()
	{
		setName("LimitedActions2");
		
		MCTSTools.setActions(new boolean[][]{
			//            LEFT   RIGHT  DOWN   JUMP   SPEED
			new boolean[]{false, true , false, false, false}, //Right
			new boolean[]{false, true , false, true , false}, //Right Jump
			new boolean[]{false, true , false, false, true }, //Right Speed
			new boolean[]{false, true , false, true , true }, //Right Jump Speed
			
			new boolean[]{true , false, false, false, false}, //Left
			new boolean[]{true , false, false, true , false}, //Left Jump
			new boolean[]{true , false, false, false, true }, //Left Speed
			new boolean[]{true , false, false, true , true }, //Left Jump Speed
			
			new boolean[]{false, false, false, true , false}, //Jump
			new boolean[]{false, false, false, true , true } //Jump Speed
			//new boolean[]{false, false, true , false, false}, //Duck
			//new boolean[]{false, false, true , false, true }  //Duck Speed
		});
		
	}
}
