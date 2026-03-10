package com.pokemon.engine;

import com.pokemon.model.Move;
import com.pokemon.model.Move.Category;
import com.pokemon.model.Move.Effect;
import com.pokemon.model.Pokemon;
import com.pokemon.model.Pokemon.Status;
import com.pokemon.model.PokemonType;

import java.util.List;
import java.util.Random;

public class BattleEngine {

    public static final int XP_BASE = 50;
    private static final Random rand = new Random();

    public record BattleResult(int damage, String message, boolean hit, boolean critical) {}

    /** Execute one move from attacker to defender. Returns result with damage and message. */
    public static BattleResult executeMove(Pokemon attacker, Move move, Pokemon defender) {
        // Check if move misses
        if (move.getAccuracy() > 0) {
            int roll = rand.nextInt(100);
            if (roll >= move.getAccuracy()) {
                return new BattleResult(0, attacker.getName() + "'s attack missed!", false, false);
            }
        }

        move.usePP();

        // Status moves
        if (move.getCategory() == Category.STATUS) {
            return handleStatusMove(attacker, move, defender);
        }

        // Damage calculation
        boolean isCritical = rand.nextInt(16) == 0;
        double critMultiplier = isCritical ? 1.5 : 1.0;

        double attackStat, defenseStat;
        if (move.getCategory() == Category.PHYSICAL) {
            attackStat = attacker.getEffectiveAttack();
            defenseStat = defender.getEffectiveDefense();
            // Burn halves physical attack
            if (attacker.getStatus() == Status.BURN) attackStat /= 2;
        } else {
            attackStat = attacker.getEffectiveSpAttack();
            defenseStat = defender.getEffectiveSpDefense();
        }

        // Base damage formula
        double base = ((2.0 * attacker.getLevel() / 5.0 + 2) * move.getPower()
                * (attackStat / defenseStat) / 50.0) + 2;

        // Type effectiveness
        double typeEff1 = PokemonType.getEffectiveness(move.getType(), defender.getType1());
        double typeEff2 = defender.getType2() != PokemonType.NONE
                ? PokemonType.getEffectiveness(move.getType(), defender.getType2()) : 1.0;
        double typeMultiplier = typeEff1 * typeEff2;

        // STAB (Same Type Attack Bonus)
        boolean stab = (move.getType() == attacker.getType1() ||
                        move.getType() == attacker.getType2());
        double stabMultiplier = stab ? 1.5 : 1.0;

        // Random factor (0.85 - 1.00)
        double randomFactor = (rand.nextInt(16) + 85) / 100.0;

        int damage = (int)(base * typeMultiplier * stabMultiplier * critMultiplier * randomFactor);
        damage = Math.max(1, damage);

        // Zero effectiveness
        if (typeMultiplier == 0.0) {
            return new BattleResult(0, "It doesn't affect " + defender.getName() + "...", true, false);
        }

        defender.takeDamage(damage);

        StringBuilder msg = new StringBuilder();
        msg.append(attacker.getName()).append(" used ").append(move.getName()).append("!");

        if (isCritical) msg.append("\nA critical hit!");
        if (typeMultiplier > 1.0) msg.append("\nIt's super effective!");
        else if (typeMultiplier < 1.0) msg.append("\nIt's not very effective...");

        // Drain moves - heal attacker
        if (move.getEffect() == Effect.DRAIN) {
            int healed = damage / 2;
            attacker.heal(healed);
            msg.append("\n").append(attacker.getName()).append(" absorbed ").append(healed).append(" HP!");
        }

        // Secondary effect chance
        if (move.getEffect() != Effect.NONE && move.getEffect() != Effect.DRAIN) {
            if (rand.nextInt(100) < move.getEffectChance()) {
                String effectMsg = applySecondaryEffect(defender, move.getEffect());
                if (!effectMsg.isEmpty()) msg.append("\n").append(effectMsg);
            }
        }

        return new BattleResult(damage, msg.toString(), true, isCritical);
    }

    private static BattleResult handleStatusMove(Pokemon attacker, Move move, Pokemon defender) {
        StringBuilder msg = new StringBuilder();
        msg.append(attacker.getName()).append(" used ").append(move.getName()).append("!");

        switch (move.getEffect()) {
            case LOWER_ATK -> { defender.modifyAtk(-1); msg.append("\n").append(defender.getName()).append("'s Attack fell!"); }
            case LOWER_DEF -> { defender.modifyDef(-1); msg.append("\n").append(defender.getName()).append("'s Defense fell!"); }
            case LOWER_SPD -> { defender.modifySpd(-1); msg.append("\n").append(defender.getName()).append("'s Speed fell!"); }
            case RAISE_ATK -> { attacker.modifyAtk(2); msg.append("\n").append(attacker.getName()).append("'s Attack sharply rose!"); }
            case RAISE_DEF -> { attacker.modifyDef(2); msg.append("\n").append(attacker.getName()).append("'s Defense sharply rose!"); }
            case RAISE_SP_ATK -> { attacker.modifySpAtk(2); msg.append("\n").append(attacker.getName()).append("'s Sp. Atk sharply rose!"); }
            case SLEEP -> {
                if (defender.getStatus() == Status.NONE) {
                    defender.applyStatus(Status.SLEEP);
                    msg.append("\n").append(defender.getName()).append(" fell asleep!");
                } else {
                    msg.append("\nIt didn't affect ").append(defender.getName()).append("!");
                }
            }
            case PARALYZE -> {
                if (defender.getStatus() == Status.NONE) {
                    defender.applyStatus(Status.PARALYSIS);
                    msg.append("\n").append(defender.getName()).append(" is now paralyzed!");
                }
            }
            case POISON -> {
                if (defender.getStatus() == Status.NONE) {
                    defender.applyStatus(Status.POISON);
                    msg.append("\n").append(defender.getName()).append(" is poisoned!");
                }
            }
            case CONFUSE -> {
                msg.append("\n").append(defender.getName()).append(" became confused!");
            }
            default -> msg.append("\n").append(attacker.getName()).append(" used ").append(move.getName()).append("!");
        }
        return new BattleResult(0, msg.toString(), true, false);
    }

    private static String applySecondaryEffect(Pokemon defender, Effect effect) {
        if (defender.getStatus() != Status.NONE && isStatusEffect(effect)) return "";
        return switch (effect) {
            case BURN    -> { defender.applyStatus(Status.BURN);     yield defender.getName() + " was burned!"; }
            case POISON  -> { defender.applyStatus(Status.POISON);   yield defender.getName() + " was poisoned!"; }
            case PARALYZE-> { defender.applyStatus(Status.PARALYSIS);yield defender.getName() + " is paralyzed!"; }
            case FREEZE  -> { defender.applyStatus(Status.FREEZE);   yield defender.getName() + " was frozen!"; }
            case FLINCH  -> defender.getName() + " flinched!";
            case LOWER_DEF -> { defender.modifyDef(-1); yield defender.getName() + "'s Defense fell!"; }
            default -> "";
        };
    }

    private static boolean isStatusEffect(Effect e) {
        return e == Effect.BURN || e == Effect.POISON || e == Effect.PARALYZE || e == Effect.FREEZE;
    }

    /** Calculate XP gained from defeating a wild pokemon */
    public static int calculateXP(Pokemon defeated, boolean isWild) {
        int base = XP_BASE * defeated.getLevel() / 7;
        return isWild ? base : (base * 3 / 2); // trainer battles give 1.5x
    }

    /** AI move selection: picks the best move for the enemy */
    public static Move selectAIMove(Pokemon ai, Pokemon target) {
        List<Move> usableMoves = ai.getMoves().stream()
                .filter(Move::isUsable).toList();
        if (usableMoves.isEmpty()) return MoveDatabase.getStruggle();

        // Score each move
        Move best = null;
        double bestScore = -1;

        for (Move m : usableMoves) {
            double score = scoreMoveForAI(ai, m, target);
            if (score > bestScore) {
                bestScore = score;
                best = m;
            }
        }
        return best != null ? best : usableMoves.get(0);
    }

    private static double scoreMoveForAI(Pokemon user, Move move, Pokemon target) {
        if (move.getCategory() == Category.STATUS) return 0.5 + Math.random() * 0.3;

        double score = move.getPower();

        // Type effectiveness
        double typeEff = PokemonType.getEffectiveness(move.getType(), target.getType1());
        if (target.getType2() != PokemonType.NONE) {
            typeEff *= PokemonType.getEffectiveness(move.getType(), target.getType2());
        }
        score *= typeEff;

        // STAB
        if (move.getType() == user.getType1() || move.getType() == user.getType2()) score *= 1.5;

        // Accuracy penalty
        if (move.getAccuracy() < 100) score *= (move.getAccuracy() / 100.0);

        // Random factor to add some unpredictability
        score *= (0.8 + Math.random() * 0.4);

        return score;
    }

    private static class MoveDatabase {
        static Move getStruggle() {
            return new Move("Struggle", PokemonType.NORMAL, Category.PHYSICAL, 50, 100, 999, "Desperate attack.");
        }
    }

    /** Apply end-of-turn status damage and return message */
    public static String applyEndOfTurn(Pokemon pokemon) {
        if (pokemon.isFainted()) return "";
        int hpBefore = pokemon.getCurrentHp();
        pokemon.applyEndOfTurnStatus();
        int damage = hpBefore - pokemon.getCurrentHp();
        if (damage > 0) {
            return switch (pokemon.getStatus()) {
                case BURN   -> pokemon.getName() + " is hurt by its burn! (-" + damage + " HP)";
                case POISON -> pokemon.getName() + " is hurt by poison! (-" + damage + " HP)";
                default     -> "";
            };
        }
        return "";
    }
}
