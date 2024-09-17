package dev.luminous.api.utils.math;

public enum Easing
{
    Linear
            {
                @Override
                public double ease(double progress)
                {
                    return progress;
                }
            },
    SineOut
            {
                @Override
                public double ease(double progress)
                {
                    return Math.sin((progress * Math.PI) / 2);
                }
            },
    SineInOut
            {
                @Override
                public double ease(double progress)
                {
                    return -(Math.cos(Math.PI * progress) - 1) / 2;
                }
            },
    CubicIn
            {
                @Override
                public double ease(double progress)
                {
                    return Math.pow(progress, 3);
                }
            },
    CubicOut
            {
                @Override
                public double ease(double progress)
                {
                    return 1 - Math.pow(1 - progress, 3);
                }
            },
    CubicInOut
            {
                @Override
                public double ease(double progress)
                {
                    return progress < 0.5 ? 4 * Math.pow(progress, 3) : 1 - Math.pow(-2 * progress + 2, 3) / 2;
                }
            },
    QuadIn
            {
                @Override
                public double ease(double progress)
                {
                    return Math.pow(progress, 2);
                }
            },
    QuadOut
            {
                @Override
                public double ease(double progress)
                {
                    return 1 - (1 - progress) * (1 - progress);
                }
            },
    QuadInOut
            {
                @Override
                public double ease(double progress)
                {
                    return progress < 0.5 ? 8 * Math.pow(progress, 4) : 1 - Math.pow(-2 * progress + 2, 4) / 2;
                }
            },
    QuartIn
            {
                @Override
                public double ease(double progress)
                {
                    return Math.pow(progress, 4);
                }
            },
    QuartOut
            {
                @Override
                public double ease(double progress)
                {
                    return 1 - Math.pow(1 - progress, 4);
                }
            },
    QuartInOut
            {
                @Override
                public double ease(double progress)
                {
                    return progress < 0.5 ? 8 * Math.pow(progress, 4) : 1 - Math.pow(-2 * progress + 2, 4) / 2;
                }
            },
    QuintIn
            {
                @Override
                public double ease(double progress)
                {
                    return Math.pow(progress, 5);
                }
            },
    QuintOut
            {
                @Override
                public double ease(double progress)
                {
                    return 1 - Math.pow(1 - progress, 5);
                }
            },
    QuintInOut
            {
                @Override
                public double ease(double progress)
                {
                    return progress < 0.5 ? 16 * Math.pow(progress, 5) : 1 - Math.pow(-2 * progress + 2, 5) / 2;
                }
            },
    CircIn
            {
                @Override
                public double ease(double progress)
                {
                    return 1 - Math.sqrt(1 - Math.pow(progress, 2));
                }
            },
    CircOut
            {
                @Override
                public double ease(double progress)
                {
                    return Math.sqrt(1 - Math.pow(progress - 1, 2));
                }
            },
    CircInOut
            {
                @Override
                public double ease(double progress)
                {
                    return progress < 0.5 ? (1 - Math.sqrt(1 - Math.pow(2 * progress, 2))) / 2 :  (Math.sqrt(1 - Math.pow(-2 * progress + 2, 2)) + 1) / 2;
                }
            },
    Expo
            {
                @Override
                public double ease(double progress)
                {
                    return progress == 0 ? 0 : progress == 1 ? 1 : progress < 0.5 ? Math.pow(2, 20 * progress - 10) / 2 : (2 - Math.pow(2, -20 * progress + 10)) / 2;
                }
            },
    BackOut
            {
                @Override
                public double ease(double progress)
                {
                    double c1 = 1.70158;
                    double c3 = c1 + 1;

                    return 1 + c3 * Math.pow(progress - 1, 3) + c1 * Math.pow(progress - 1, 2);
                }
            },
    BackInOut
            {
                @Override
                public double ease(double progress)
                {
                    return progress < 0.5 ? (Math.pow(2 * progress, 2) * (((1.70158 * 1.525) + 1) * 2 * progress - (1.70158 * 1.525))) / 2 : (Math.pow(2 * progress - 2, 2) * (((1.70158 * 1.525) + 1) * (progress * 2 - 2) + (1.70158 * 1.525)) + 2) / 2;
                }
            },
    Bounce
            {
                @Override
                public double ease(double progress)
                {
                    if (progress < 1 / 2.75) {
                        return 7.5625 * progress * progress;
                    } else if (progress < 2 / 2.75) {
                        progress -= 1.5 / 2.75;
                        return 7.5625 * progress * progress + 0.75;
                    } else if (progress < 2.5 / 2.75) {
                        progress -= 2.25 / 2.75;
                        return 7.5625 * progress * progress + 0.9375;
                    } else {
                        progress -= 2.625 / 2.75;
                        return 7.5625 * progress * progress + 0.984375;
                    }
                }
            };

    public abstract double ease(double progress);
}