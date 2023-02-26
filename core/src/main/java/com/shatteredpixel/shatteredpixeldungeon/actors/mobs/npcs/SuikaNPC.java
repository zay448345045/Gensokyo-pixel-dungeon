package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.UFOSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Golem;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Monk;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.encounters.SuikaEnc;
import com.shatteredpixel.shatteredpixeldungeon.items.encounters.MarisaEnc;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DwarfToken;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.EndlessAlcohol;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.ForestLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ImpSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndImp;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuest;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class SuikaNPC extends NPC {

	{
		spriteClass = ImpSprite.class;

		properties.add(Property.IMMOVABLE);
	}
	

	@Override
	protected boolean act() {
		if (!Quest.given && Dungeon.level.heroFOV[pos]) {
			Notes.add( Notes.Landmark.SUIKA );
		} 
		return super.act();
	}
	
	@Override
	public int defenseSkill( Char enemy ) {
		return INFINITE_EVASION;
	}
	
	@Override
	public void damage( int dmg, Object src ) {
	}
	
	@Override
	public void add( Buff buff ) {
	}
	
	@Override
	public boolean reset() {
		return true;
	}
	
	@Override
	public boolean interact(Char c) {
		
		sprite.turnTo( pos, Dungeon.hero.pos );

		if (c != Dungeon.hero){
			return true;
		}

		//Have quest
		if (Quest.given) {
			PotionOfHealing poh = Dungeon.hero.belongings.getItem( PotionOfHealing.class );
			//Finished
			if (Quest.completed){
				switch(Random.IntRange(1,4)){
					default:
					case 1:
						sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "quest_finished1"));
						break;
					case 2:
						sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "quest_finished2"));
						break;
					case 3:
						sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "quest_finished3"));
						break;
					case 4:
						sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "quest_finished4"));
						break;
				}
			// fnished now
			}else if (poh != null && (poh.quantity() >= 2) && !Quest.completed) {
				Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						poh.detach(Dungeon.hero.belongings.backpack);
						poh.detach(Dungeon.hero.belongings.backpack);
						SuikaNPC.Quest.complete();
						tell(Messages.get(SuikaNPC.class, "quest_3"));
						EndlessAlcohol alcohol = new EndlessAlcohol();
						alcohol.quantity(3).collect();

						if (!Catalog.isSeen(SuikaEnc.class)) {
							// Catalog.setSeen(SuikaEnc.class);
							Dungeon.level.drop(new MarisaEnc(), Dungeon.hero.pos ).sprite.drop();
						}
					}
				});
			//Not finish
			} else {
				tell(Messages.get(this, "quest_2", Dungeon.hero.name()));
			}
		//No quest
		} else {
			if (Catalog.isSeen(SuikaEnc.class)) tell(Messages.get(this, "quest_1", Dungeon.hero.name()));
			else tell(Messages.get(this, "quest_1_firsttime", Dungeon.hero.name()));
			Quest.given = true;
			Quest.completed = false;
			Notes.add( Notes.Landmark.SUIKA );
		}

		return true;
	}
	
	private void tell( String text ) {
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.show( new WndQuest( SuikaNPC.this, text ));
			}
		});
	}
	
	public void flee() {
		yell( Messages.get(this, "cya", Dungeon.hero.name()) );
		destroy();
		sprite.die();
	}

	public static class Quest {
		
		private static boolean alternative;
		
		private static boolean spawned;
		private static boolean given;
		private static boolean completed;
		
		// public static Ring reward;
		
		public static void reset() {
			spawned		= false;
			given		= false;
			completed	= false;
			alternative	= false;
		}
		
		private static final String NODE		= "suika";
		
		private static final String ALTERNATIVE	= "suika_a";
		private static final String SPAWNED		= "suika_s";
		private static final String GIVEN		= "suika_g";
		private static final String COMPLETED	= "suika_c";
		
		public static void storeInBundle( Bundle bundle ) {
			
			Bundle node = new Bundle();
			
			node.put( SPAWNED, spawned );
			
			if (spawned) {
				node.put( ALTERNATIVE, alternative );
				
				node.put( GIVEN, given );
				node.put( COMPLETED, completed );
			}
			
			bundle.put( NODE, node );
		}
		
		public static void restoreFromBundle( Bundle bundle ) {

			Bundle node = bundle.getBundle( NODE );
			
			if (!node.isNull() && (spawned = node.getBoolean( SPAWNED ))) {
				alternative	= node.getBoolean( ALTERNATIVE );
				
				given = node.getBoolean( GIVEN );
				completed = node.getBoolean( COMPLETED );
			}
		}
		
		public static void spawn( ForestLevel level ) {
			// TODO Dungeon.depth > 99 to disable her spawn
			// Will try to spawn her lying somewhere between tree
			// She doesn't spawn outside so these are irrelevant for now, she spawn in her room
			// if (!spawned && Dungeon.depth > 99 && Random.Int( 4 - Dungeon.depth ) == 0) {
				
			// 	SuikaNPC npc = new SuikaNPC();
			// 	do {
			// 		npc.pos = level.randomRespawnCell( npc );
			// 	} while (
			// 			npc.pos == -1 ||
			// 			level.heaps.get( npc.pos ) != null ||
			// 			level.traps.get( npc.pos) != null ||
			// 			level.findMob( npc.pos ) != null ||
			// 			//Suika doesn't move, so she cannot obstruct a passageway
			// 			!(level.passable[npc.pos + PathFinder.CIRCLE4[0]] && level.passable[npc.pos + PathFinder.CIRCLE4[2]]) ||
			// 			!(level.passable[npc.pos + PathFinder.CIRCLE4[1]] && level.passable[npc.pos + PathFinder.CIRCLE4[3]]));
			// 	level.mobs.add( npc );
			// 	spawned = true;
			// 	given = false;
			// }
		}
		public static void complete() {
			// reward = null;
			completed = true;

			Statistics.questScores[3] = 500;
			Notes.remove( Notes.Landmark.SUIKA );
		}
		
		public static boolean isCompleted() {
			return completed;
		}
	}
}
