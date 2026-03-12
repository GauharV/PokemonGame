                g.fillPolygon(axp, ayp, 3);

                // Destination label — floating sign above/beside the exit tile
                String dest = getDestination(type);
                if (dest != null) {
                    g.setFont(new Font("Arial Black", Font.BOLD, 11));
                    FontMetrics fm = g.getFontMetrics();
                    int lw = fm.stringWidth(dest);
                    int lh = 18;
                    int lx = tx + TILE/2 - lw/2 - 6;
                    int ly = ty - lh - 4;  // float above the tile
                    // Sign background
                    g.setColor(new Color(20, 20, 20, 210));
                    g.fillRoundRect(lx, ly, lw + 12, lh, 6, 6);
                    g.setColor(new Color(100, 220, 100));
                    g.setStroke(new BasicStroke(1.5f));
                    g.drawRoundRect(lx, ly, lw + 12, lh, 6, 6);
                    // Arrow direction prefix
                    String arrow = switch(type) {
                        case T_EXIT_N -> "↑ ";
                        case T_EXIT_S -> "↓ ";
                        case T_EXIT_E -> "→ ";
                        case T_EXIT_W -> "← ";
                        default -> "";
                    };
                    g.setColor(new Color(255, 230, 80));
                    g.drawString(arrow + dest, lx + 6, ly + lh - 4);
                }
            }
