/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Fury;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class KoishiHat extends Artifact {

	{
		image = ItemSpriteSheet.ARTIFACT_KOISHIHAT;
		levelCap = 10;
        exp = 0;
        unique = true;
        bones = false;
		imaginary_friend = 0;
		keptThoughLostInvent = true;
	}

	public float total = 15f;
	public float cooldown = 15f;
    public boolean invis = false;

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions( hero );
		actions.remove( AC_THROW );
		actions.remove( AC_DROP );
		return actions;
	}

    @Override
	public int value() {
		return 0;
	}

    @Override
	protected ArtifactBuff passiveBuff() {
		return new Koishibuff();
	}
	
	@Override
	public String desc() {
		String desc = super.desc();
		return desc;
	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (Dungeon.hero.buff(Invisibility.class) != null){
			Dungeon.hero.buff(Invisibility.class).detach();
		}
		Buff.affect(Dungeon.hero, Fury.class);
		Buff.affect(Dungeon.hero, Doom.class);
		return super.doUnequip(hero, collect, single);
	}

	@Override
	public boolean doEquip(Hero hero) {
		if(Dungeon.hero.buff(Fury.class) != null){
			GLog.w("Koishi hasn't calm down yet!");
			return false;
		} else {
			if (Dungeon.hero.buff(Doom.class) != null){
				Dungeon.hero.buff(Doom.class).detach();
			};
			return super.doEquip(hero);
		}
	}

	private static final String TURN_TILL_INVIS =        "turn_till_invis";
	private static final String TURN_TO_INVIS =        "turn_to_invis";
	@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( TURN_TILL_INVIS, passiveBuff.koishiDummyCoolDown() );
			bundle.put( TURN_TO_INVIS, passiveBuff.koishiDummyTotal() );
		}
		
		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			cooldown = bundle.getFloat( TURN_TILL_INVIS );
			total = bundle.getFloat( TURN_TO_INVIS );
		}

    public class Koishibuff extends ArtifactBuff{
        public float turn_to_invis = total - imaginary_friend; //total turn needed to invis
        public float turn_till_invis = cooldown;					 //how many turn left till invis

		@Override
		public float koishiDummyCoolDown() {
			return turn_till_invis;
		}

		@Override
		public float koishiDummyTotal() {
			return turn_to_invis;
		}

        @Override
        public boolean act() {
            if (turn_till_invis > 0){
                turn_till_invis--;
            }

			if(Dungeon.hero.curAction instanceof HeroAction.Attack){
				turn_till_invis = turn_to_invis;
			}

			if(invis == true && Dungeon.hero.buff(Invisibility.class) == null){
                turn_till_invis = turn_to_invis;
                invis = false;
            }

            if (turn_till_invis == 0){
                Buff.prolong(Dungeon.hero, Invisibility.class, 514f);
                invis = true;
            }

            spend(TICK);
            return true;
        }

        @Override
		public String toString() {
			return  "Closed Mind";
		}

		@Override
		public String desc() {
			int chance = 40 + 10*Dungeon.hero.pointsInTalent(Talent.LEARNING);
			return String.format("Koishi closed her mind, which makes her presence cannot be noticed by other beings unless she allows it. After %1$.1f turns of not attacking, she will turn invisible. \n\nTurns visible remaining: %2$.1f.\n\nHowever, by doing so Koishi have _%3$d%% chance_ to only gain 50%% exp instead (rounded down).", turn_to_invis, turn_till_invis, chance);
		}

		@Override
		public int icon() {
			return BuffIndicator.KOISHI;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.brightness(0.6f);
		}

		@Override
		public float iconFadePercent() {
			return (15f - turn_till_invis) / 15f;
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(Math.round(turn_till_invis));
		}

    }
}
