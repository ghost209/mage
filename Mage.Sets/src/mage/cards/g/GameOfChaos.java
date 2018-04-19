/*
 *  Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ``AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of BetaSteward_at_googlemail.com.
 */
package mage.cards.g;

import java.util.UUID;
import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetOpponent;

/**
 *
 * @author escplan9 (Derek Monturo - dmontur1 at gmail dot com)
 */
public class GameOfChaos extends CardImpl {

    public GameOfChaos(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.SORCERY},"{R}{R}{R}");

        // Flip a coin. 
        // If you win the flip, you gain 1 life and target opponent loses 1 life, and you decide whether to flip again. 
        // If you lose the flip, you lose 1 life and that opponent gains 1 life, and that player decides whether to flip again. 
        // Double the life stakes with each flip.
        this.getSpellAbility().addEffect(new GameOfChaosEffect());
        this.getSpellAbility().addTarget(new TargetOpponent());
    }

    public GameOfChaos(final GameOfChaos card) {
        super(card);
    }

    @Override
    public GameOfChaos copy() {
        return new GameOfChaos(this);
    }
}

class GameOfChaosEffect extends OneShotEffect {
    
    public GameOfChaosEffect() {
        super(Outcome.Detriment);
        this.staticText = "Flip a coin. If you win the flip, you gain 1 life and target opponent loses 1 life, and you decide whether to flip again. If you lose the flip, you lose 1 life and that opponent gains 1 life, and that player decides whether to flip again. Double the life stakes with each flip.";
    }
    
    public GameOfChaosEffect(final GameOfChaosEffect effect) {
        super(effect);
    }
    
    @Override
    public GameOfChaosEffect copy() {
        return new GameOfChaosEffect(this);
    }
    
    @Override
    public boolean apply(Game game, Ability source) {
        Player you = game.getPlayer(source.getControllerId());
        Player targetOpponent = game.getPlayer(getTargetPointer().getFirst(game, source));
        
        if (you != null && targetOpponent != null) {
            
            boolean continueFlipping = true;
            boolean youWonFlip = you.flipCoin(game); // controller flips first
            boolean youWonLastFlip = false; // tracks if you won the flip last, negation of it means opponent won last
            int lifeAmount = 1; // starts stakes with 1 life
            
            while (continueFlipping) {                
                if (youWonFlip) { // flipper of coin wins, flipper gain 1 and non-flipper loses 1
                    handleLifeChangesFromFlip(game, you, targetOpponent, lifeAmount);
                    if (!cannotContinueFlipping(you, targetOpponent)) {
                        continueFlipping = you.chooseUse(outcome, "You gained " + lifeAmount + " life! Flip again for double the life stakes?", source, game);
                        youWonLastFlip = true;
                    }
                } else { // non-flipper wins, flipper lose 1 and non-flipper gains 1
                    handleLifeChangesFromFlip(game, targetOpponent, you, lifeAmount);
                    if (!cannotContinueFlipping(you, targetOpponent)) {
                        continueFlipping = targetOpponent.chooseUse(outcome, "You gained " + lifeAmount + " life! Flip again for double the life stakes?", source, game);
                        youWonLastFlip = false;
                    }
                }
                
                if (cannotContinueFlipping(you, targetOpponent))
                    continueFlipping = false;
                                
                if (continueFlipping) {
                    lifeAmount *= 2; // double the life each time
                    youWonFlip = youWonLastFlip ? you.flipCoin(game) : !targetOpponent.flipCoin(game); // negate the opponent's results for proper evaluation of if you won in next iteration
                }
            }

            return true;
        }
        return false;
    }
        
    private void handleLifeChangesFromFlip(Game game, Player playerGainingLife, Player playerLosingLife, int lifeAmount) {        
        playerGainingLife.gainLife(lifeAmount, game, source);
        playerLosingLife.loseLife(lifeAmount, game, false);
    }
    
    private boolean cannotContinueFlipping(Player you, Player opponent) {
        return (!you.canRespond() || !opponent.canRespond() || (you.canLoseByZeroOrLessLife() && you.getLife() <= 0) || (opponent.canLoseByZeroOrLessLife() && opponent.getLife() <= 0));        
    }
}