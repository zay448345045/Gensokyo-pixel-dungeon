/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
 *
 * Gensokyo Pixel Dungeon
 * Copyright (C) 2022-2023 GrampHoang
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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.PathFinder;

public class LusanaViolin extends GhostbandWeapon {

	{
		image = ItemSpriteSheet.LUSANA_VIOLIN;
		hitSound = Assets.Sounds.HIT;
		hitSoundPitch = 1f;

		hitCounter = 0;
    }

    @Override
	public void activateAttackSkill(Char attacker, Char defender) {
        for (int i : PathFinder.NEIGHBOURS9){
            CellEmitter.get(i + defender.pos).start( Speck.factory( Speck.NOTE ), 0.2f, 3 );
            Char ch = Actor.findChar(defender.pos + i);
            //Exist and not same alignment, not the current user
            if (ch != null && ch.alignment != attacker.alignment && ch != defender){
                ch.damage(skillDamage(), attacker);
            }
        }
	}

    @Override
    public String skillInfo(){
		return Messages.get(this, "skill_desc", chargeGain, chargeNeed, min(), max());
	}
}
