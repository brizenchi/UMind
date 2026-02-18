<!-- Focus Dashboard -->
<!DOCTYPE html>
<html class="dark" lang="en"><head>
<meta charset="utf-8"/>
<link crossorigin="" href="https://fonts.gstatic.com/" rel="preconnect"/>
<link as="style" href="https://fonts.googleapis.com/css2?display=swap&amp;family=Inter%3Awght%40400%3B500%3B700%3B900&amp;family=Noto+Sans%3Awght%40400%3B500%3B700%3B900" onload="this.rel='stylesheet'" rel="stylesheet"/>
<title>Stitch Design</title>
<link href="data:image/x-icon;base64," rel="icon" type="image/x-icon"/>
<script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
<script id="tailwind-config">
        tailwind.config = {
          darkMode: "class",
          theme: {
            extend: {
              colors: {
                "primary": "#1173d4",
                "background-light": "#f6f7f8",
                "background-dark": "#101922",
              },
              fontFamily: {
                "display": ["Inter"]
              },
              borderRadius: {"DEFAULT": "0.5rem", "lg": "1rem", "xl": "1.5rem", "full": "9999px"},
            },
          },
        }
      </script>
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined" rel="stylesheet"/>
<style>
    body {
      min-height: max(884px, 100dvh);
    }
  </style>
<style>
    body {
      min-height: max(884px, 100dvh);
    }
  </style>
  </head>
<body class="font-display">
<div class="relative flex h-auto min-h-screen w-full flex-col bg-background-light dark:bg-background-dark justify-between">
<div class="flex-grow">
<header class="flex items-center p-4 pb-2 justify-between">
<button class="text-slate-800 dark:text-white flex size-10 items-center justify-center rounded-full">
<span class="material-symbols-outlined text-2xl">
                menu
                </span>
</button>
<h1 class="text-slate-900 dark:text-white text-lg font-bold">Focus Strategies</h1>
<div class="size-10"></div>
</header>
<main class="p-4">
<div class="mb-8">
<h2 class="text-2xl font-bold text-slate-900 dark:text-white mb-4">Your Strategies</h2>
<div class="space-y-4">
<div class="bg-slate-100 dark:bg-slate-800/50 p-4 rounded-lg">
<div class="flex items-center justify-between">
<div>
<p class="font-bold text-slate-900 dark:text-white">Work Focus</p>
<p class="text-sm text-slate-600 dark:text-slate-400">Time-based, Duration-based</p>
</div>
<div class="flex items-center gap-2">
<button class="text-slate-600 dark:text-slate-300 flex size-9 items-center justify-center rounded-full hover:bg-slate-200 dark:hover:bg-slate-700">
<span class="material-symbols-outlined text-xl">
                                edit
                            </span>
</button>
<button class="text-slate-600 dark:text-slate-300 flex size-9 items-center justify-center rounded-full hover:bg-slate-200 dark:hover:bg-slate-700">
<span class="material-symbols-outlined text-xl">
                                delete
                            </span>
</button>
</div>
</div>
</div>
<div class="bg-slate-100 dark:bg-slate-800/50 p-4 rounded-lg">
<div class="flex items-center justify-between">
<div>
<p class="font-bold text-slate-900 dark:text-white">Evening Wind Down</p>
<p class="text-sm text-slate-600 dark:text-slate-400">Time-based, Frequency-based</p>
</div>
<div class="flex items-center gap-2">
<button class="text-slate-600 dark:text-slate-300 flex size-9 items-center justify-center rounded-full hover:bg-slate-200 dark:hover:bg-slate-700">
<span class="material-symbols-outlined text-xl">
                                edit
                            </span>
</button>
<button class="text-slate-600 dark:text-slate-300 flex size-9 items-center justify-center rounded-full hover:bg-slate-200 dark:hover:bg-slate-700">
<span class="material-symbols-outlined text-xl">
                                delete
                            </span>
</button>
</div>
</div>
</div>
<div class="bg-slate-100 dark:bg-slate-800/50 p-4 rounded-lg">
<div class="flex items-center justify-between">
<div>
<p class="font-bold text-slate-900 dark:text-white">Study Session</p>
<p class="text-sm text-slate-600 dark:text-slate-400">Duration-based</p>
</div>
<div class="flex items-center gap-2">
<button class="text-slate-600 dark:text-slate-300 flex size-9 items-center justify-center rounded-full hover:bg-slate-200 dark:hover:bg-slate-700">
<span class="material-symbols-outlined text-xl">
                                edit
                            </span>
</button>
<button class="text-slate-600 dark:text-slate-300 flex size-9 items-center justify-center rounded-full hover:bg-slate-200 dark:hover:bg-slate-700">
<span class="material-symbols-outlined text-xl">
                                delete
                            </span>
</button>
</div>
</div>
</div>
</div>
</div>
<div class="text-center">
<button class="w-full text-center py-4 px-6 rounded-lg bg-primary text-white font-bold flex items-center justify-center gap-2">
<span class="material-symbols-outlined">
              add_circle
            </span>
<span>Add New Strategy</span>
</button>
</div>
</main>
</div>
<footer class="sticky bottom-0 bg-background-light/80 dark:bg-background-dark/80 backdrop-blur-sm border-t border-slate-200 dark:border-slate-800">
<nav class="flex justify-around py-2">
<a class="flex flex-col items-center gap-1 p-2 rounded-lg text-primary" href="#">
<span class="material-symbols-outlined">
                bolt
            </span>
<span class="text-xs font-medium">Focus</span>
</a>
<a class="flex flex-col items-center gap-1 p-2 rounded-lg text-slate-500 dark:text-slate-400" href="#">
<span class="material-symbols-outlined">
                bar_chart
                </span>
<span class="text-xs font-medium">Statistics</span>
</a>
<a class="flex flex-col items-center gap-1 p-2 rounded-lg text-slate-500 dark:text-slate-400" href="#">
<span class="material-symbols-outlined">
                settings
                </span>
<span class="text-xs font-medium">Settings</span>
</a>
</nav>
</footer>
</div>

</body></html>

<!-- App Selection & Blocking -->
<!DOCTYPE html>
<html class="dark" lang="en"><head>
<meta charset="utf-8"/>
<meta content="width=device-width, initial-scale=1.0" name="viewport"/>
<title>App Blocking</title>
<script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
<link href="https://fonts.googleapis.com" rel="preconnect"/>
<link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect"/>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&amp;display=swap" rel="stylesheet"/>
<script>
        tailwind.config = {
            darkMode: "class",
            theme: {
                extend: {
                    colors: {
                        "primary": "#1173d4",
                        "background-light": "#f6f7f8",
                        "background-dark": "#101922",
                    },
                    fontFamily: {
                        "display": ["Inter", "sans-serif"]
                    },
                    borderRadius: {
                        "DEFAULT": "0.5rem",
                        "lg": "1rem",
                        "xl": "1.5rem",
                        "full": "9999px"
                    },
                },
            },
        }
    </script>
<style>
        .icon {
            font-variation-settings:
                'FILL' 0,
                'wght' 400,
                'GRAD' 0,
                'opsz' 24
        }
    </style>
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined" rel="stylesheet"/>
<style>
    body {
      min-height: max(884px, 100dvh);
    }
    .switch {
        position: relative;
        display: inline-block;
        width: 38px;
        height: 22px;
    }
    .switch input {
        opacity: 0;
        width: 0;
        height: 0;
    }
    .slider {
        position: absolute;
        cursor: pointer;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: #ccc;
        transition: .4s;
        border-radius: 34px;
    }
    .slider:before {
        position: absolute;
        content: "";
        height: 18px;
        width: 18px;
        left: 2px;
        bottom: 2px;
        background-color: white;
        transition: .4s;
        border-radius: 50%;
    }
    input:checked + .slider {
        background-color: #1173d4;
    }
    input:checked + .slider:before {
        transform: translateX(16px);
    }
    .day-btn.active {
        background-color: #1173d4;
        color: white;
    }
  </style>
<style>
    body {
      min-height: max(884px, 100dvh);
    }
  </style>
<style>
    body {
      min-height: max(884px, 100dvh);
    }
  </style>
  </head>
<body class="bg-background-light dark:bg-background-dark font-display">
<div class="flex flex-col h-screen justify-between">
<div>
<header class="p-4 flex items-center">
<button class="text-gray-800 dark:text-white">
<span class="material-symbols-outlined">
                    arrow_back
                </span>
</button>
<h1 class="text-xl font-bold text-gray-900 dark:text-white text-center flex-grow">Blocking Settings</h1>
<div class="w-8"></div>
</header>
<main class="px-4 space-y-6">
<section>
<button class="w-full flex items-center justify-between p-4 rounded-xl bg-gray-200 dark:bg-gray-800 text-left">
<div>
<h2 class="text-lg font-bold text-gray-900 dark:text-white">Apps to Block</h2>
<p class="text-sm text-gray-500 dark:text-gray-400">3 apps selected</p>
</div>
<span class="material-symbols-outlined text-gray-500 dark:text-gray-400">
                        chevron_right
                    </span>
</button>
</section>
<section>
<h2 class="text-lg font-bold text-gray-900 dark:text-white mb-4">Rules</h2>
<div class="space-y-2">
<div class="p-4 rounded-xl bg-gray-200/50 dark:bg-gray-800/50">
<div class="flex items-center justify-between">
<div class="flex items-center gap-4">
<div class="bg-primary/10 dark:bg-primary/20 p-2 rounded-full">
<span class="material-symbols-outlined text-primary">schedule</span>
</div>
<p class="font-medium text-gray-900 dark:text-white">Time-based restrictions</p>
</div>
<label class="switch">
<input checked="" type="checkbox"/>
<span class="slider"></span>
</label>
</div>
<div class="mt-4 space-y-4">
<div class="p-3 rounded-lg bg-gray-100 dark:bg-gray-700/50">
<div class="flex items-center justify-between">
<div>
<p class="text-gray-900 dark:text-white">10:00 PM - 08:00 AM</p>
<p class="text-xs text-gray-500 dark:text-gray-400">Mon, Tue, Wed, Thu, Fri</p>
</div>
<button class="text-red-500">
<span class="material-symbols-outlined">remove_circle</span>
</button>
</div>
<div class="mt-3 flex justify-between space-x-1">
<button class="day-btn h-8 w-8 rounded-full text-xs font-semibold text-gray-700 dark:text-gray-300 active">M</button>
<button class="day-btn h-8 w-8 rounded-full text-xs font-semibold text-gray-700 dark:text-gray-300 active">T</button>
<button class="day-btn h-8 w-8 rounded-full text-xs font-semibold text-gray-700 dark:text-gray-300 active">W</button>
<button class="day-btn h-8 w-8 rounded-full text-xs font-semibold text-gray-700 dark:text-gray-300 active">T</button>
<button class="day-btn h-8 w-8 rounded-full text-xs font-semibold text-gray-700 dark:text-gray-300 active">F</button>
<button class="day-btn h-8 w-8 rounded-full text-xs font-semibold text-gray-700 dark:text-gray-300">S</button>
<button class="day-btn h-8 w-8 rounded-full text-xs font-semibold text-gray-700 dark:text-gray-300">S</button>
</div>
</div>
<div class="p-3 rounded-lg bg-gray-100 dark:bg-gray-700/50">
<div class="flex items-center justify-between">
<div>
<p class="text-gray-900 dark:text-white">12:00 PM - 01:00 PM</p>
<p class="text-xs text-gray-500 dark:text-gray-400">Sat, Sun</p>
</div>
<button class="text-red-500">
<span class="material-symbols-outlined">remove_circle</span>
</button>
</div>
<div class="mt-3 flex justify-between space-x-1">
<button class="day-btn h-8 w-8 rounded-full text-xs font-semibold text-gray-700 dark:text-gray-300">M</button>
<button class="day-btn h-8 w-8 rounded-full text-xs font-semibold text-gray-700 dark:text-gray-300">T</button>
<button class="day-btn h-8 w-8 rounded-full text-xs font-semibold text-gray-700 dark:text-gray-300">W</button>
<button class="day-btn h-8 w-8 rounded-full text-xs font-semibold text-gray-700 dark:text-gray-300">T</button>
<button class="day-btn h-8 w-8 rounded-full text-xs font-semibold text-gray-700 dark:text-gray-300">F</button>
<button class="day-btn h-8 w-8 rounded-full text-xs font-semibold text-gray-700 dark:text-gray-300 active">S</button>
<button class="day-btn h-8 w-8 rounded-full text-xs font-semibold text-gray-700 dark:text-gray-300 active">S</button>
</div>
</div>
<button class="w-full mt-2 py-2 text-primary font-semibold rounded-lg flex items-center justify-center gap-2">
<span class="material-symbols-outlined">add</span> Add time range
                                </button>
</div>
</div>
<div class="p-4 rounded-xl bg-gray-200/50 dark:bg-gray-800/50">
<div class="flex items-center justify-between">
<div class="flex items-center gap-4">
<div class="bg-primary/10 dark:bg-primary/20 p-2 rounded-full">
<span class="material-symbols-outlined text-primary">timer</span>
</div>
<p class="font-medium text-gray-900 dark:text-white">Daily usage limits</p>
</div>
<label class="switch">
<input checked="" type="checkbox"/>
<span class="slider"></span>
</label>
</div>
<div class="mt-4 flex items-center justify-between">
<p class="text-gray-900 dark:text-white">Set daily limit</p>
<div class="flex items-center gap-2">
<input class="w-16 bg-gray-100 dark:bg-gray-700 text-center rounded-md border-transparent focus:border-primary focus:ring-primary" type="text" value="1h"/>
<input class="w-16 bg-gray-100 dark:bg-gray-700 text-center rounded-md border-transparent focus:border-primary focus:ring-primary" type="text" value="30m"/>
</div>
</div>
</div>
<div class="p-4 rounded-xl bg-gray-200/50 dark:bg-gray-800/50">
<div class="flex items-center justify-between">
<div class="flex items-center gap-4">
<div class="bg-primary/10 dark:bg-primary/20 p-2 rounded-full">
<span class="material-symbols-outlined text-primary">repeat</span>
</div>
<p class="font-medium text-gray-900 dark:text-white">Frequency control</p>
</div>
<label class="switch">
<input checked="" type="checkbox"/>
<span class="slider"></span>
</label>
</div>
<div class="mt-4 flex items-center justify-between">
<p class="text-gray-900 dark:text-white">Max opens per hour</p>
<div class="flex items-center gap-2">
<button class="bg-gray-300 dark:bg-gray-700 p-1 rounded-full text-gray-800 dark:text-white">
<span class="material-symbols-outlined text-base">remove</span>
</button>
<span class="font-semibold text-gray-900 dark:text-white w-4 text-center">5</span>
<button class="bg-gray-300 dark:bg-gray-700 p-1 rounded-full text-gray-800 dark:text-white">
<span class="material-symbols-outlined text-base">add</span>
</button>
</div>
</div>
</div>
<div class="p-4 rounded-xl bg-gray-200/50 dark:bg-gray-800/50">
<div class="flex items-center justify-between">
<div class="flex items-center gap-4">
<div class="bg-primary/10 dark:bg-primary/20 p-2 rounded-full">
<span class="material-symbols-outlined text-primary">lock_open</span>
</div>
<p class="font-medium text-gray-900 dark:text-white">Temporary access</p>
</div>
<label class="switch">
<input type="checkbox"/>
<span class="slider"></span>
</label>
</div>
</div>
</div>
</section>
</main>
</div>
<footer class="p-4 bg-background-light dark:bg-background-dark">
<div class="flex gap-4">
<button class="flex-1 py-3 px-4 rounded-xl bg-gray-200 dark:bg-gray-700 text-gray-800 dark:text-white font-bold">Cancel</button>
<button class="flex-1 py-3 px-4 rounded-xl bg-primary text-white font-bold">Save</button>
</div>
</footer>
</div>

</body></html>

<!-- Welcome & Onboarding -->
<!DOCTYPE html>
<html class="dark" lang="en"><head>
<meta charset="utf-8"/>
<meta content="width=device-width, initial-scale=1.0" name="viewport"/>
<title>FocusFlow Welcome</title>
<script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
<script>
        tailwind.config = {
            darkMode: "class",
            theme: {
                extend: {
                    colors: {
                        "primary": "#1173d4",
                        "background-light": "#f6f7f8",
                        "background-dark": "#101922",
                    },
                    fontFamily: {
                        "display": ["Inter", "sans-serif"]
                    },
                    borderRadius: {
                        "DEFAULT": "0.5rem",
                        "lg": "1rem",
                        "xl": "1.5rem",
                        "full": "9999px"
                    },
                },
            },
        }
    </script>
<link href="https://fonts.googleapis.com" rel="preconnect"/>
<link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect"/>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700;900&amp;display=swap" rel="stylesheet"/>
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined" rel="stylesheet"/>
<style>
    body {
      min-height: max(884px, 100dvh);
    }
  </style>
  </head>
<body class="bg-background-light dark:bg-background-dark font-display">
<div class="flex flex-col h-screen justify-between p-6">
<div class="flex flex-col items-center text-center">
<header class="w-full py-4">
<h1 class="text-2xl font-bold text-slate-900 dark:text-white">FocusFlow</h1>
</header>
<div class="mt-16 mb-8">
<span class="material-symbols-outlined text-primary" style="font-size: 80px;">
                    hourglass
                </span>
</div>
<h2 class="text-3xl font-bold text-slate-900 dark:text-white mb-4">Welcome to FocusFlow</h2>
<p class="text-slate-600 dark:text-slate-400 max-w-sm">
                Take control of your digital well-being. We'll help you manage distractions and reclaim your focus.
            </p>
<div class="flex space-x-2 my-12">
<div class="w-3 h-3 rounded-full bg-primary"></div>
<div class="w-3 h-3 rounded-full bg-slate-300 dark:bg-slate-700"></div>
<div class="w-3 h-3 rounded-full bg-slate-300 dark:bg-slate-700"></div>
</div>
</div>
<div class="w-full">
<button class="w-full bg-primary text-white font-bold py-4 px-4 rounded-xl hover:bg-primary/90 transition-colors">
                Get Started
            </button>
<div class="h-8"></div> 
</div>
</div>

</body></html>

<!-- Trial & Subscription Prompt -->
<!DOCTYPE html>
<html class="dark" lang="en"><head>
<meta charset="utf-8"/>
<meta charset="utf-8"/>
<title>Digital Wellness App - Login</title>
<link crossorigin="" href="https://fonts.gstatic.com/" rel="preconnect"/>
<link as="style" href="https://fonts.googleapis.com/css2?display=swap&amp;family=Inter:wght@400;500;600;700&amp;family=Noto+Sans:wght@400;500;600;700" onload="this.rel='stylesheet'" rel="stylesheet"/>
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined" rel="stylesheet"/>
<script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
<script>
        tailwind.config = {
            darkMode: "class",
            theme: {
                extend: {
                    colors: {
                        "primary": "#1173d4",
                        "background-light": "#f0f2f5",
                        "background-dark": "#131313",
                        "card-light": "#ffffff",
                        "card-dark": "#1c1c1e",
                        "text-light": "#000000",
                        "text-dark": "#ffffff",
                        "text-secondary-light": "#6b7280",
                        "text-secondary-dark": "#8e8e93",
                    },
                    fontFamily: {
                        "display": ["Inter", "Noto Sans", "sans-serif"]
                    },
                    borderRadius: {
                        "DEFAULT": "0.75rem",
                        "lg": "1rem",
                        "xl": "1.5rem",
                        "full": "9999px"
                    },
                },
            },
        }
    </script>
<style>
        body {
            min-height: max(884px, 100dvh);
            background-color: #f0f2f5;
        }
        .dark body {
            background-color: #131313;
        }
        .form-input {
            background-color: #e8e8e8;
            border: none;
            border-radius: 0.75rem;
            padding: 1rem 1.25rem;
            color: #000;
        }
        .dark .form-input {
            background-color: #2c2c2e;
            color: #fff;
        }
        .form-input::placeholder {
            color: #8e8e93;
        }
        .dark .form-input::placeholder {
            color: #8e8e93;
        }
        .apple-button {
            background-color: #000000;
            color: #ffffff;
        }
        .dark .apple-button {
            background-color: #ffffff;
            color: #000000;
        }
        .google-button {
            background-color: #ffffff;
            color: #000000;
            border: 1px solid #d1d5db;
        }
        .dark .google-button {
            background-color: #2c2c2e;
            color: #ffffff;
            border: 1px solid #4a4a4a;
        }
    </style>
<style>
    body {
      min-height: max(884px, 100dvh);
    }
  </style>
  </head>
<body class="bg-background-light dark:bg-background-dark font-display text-text-light dark:text-text-dark">
<div class="flex flex-col h-screen w-full max-w-md mx-auto">
<main class="flex-1 flex flex-col justify-center items-center p-6 sm:p-8">
<div class="w-full text-center mb-10">
<span class="material-symbols-outlined text-primary text-6xl">shield</span>
<h1 class="text-3xl font-bold mt-4">Welcome Back</h1>
<p class="text-text-secondary-light dark:text-text-secondary-dark mt-2">Sign in to reclaim your focus.</p>
</div>
<div class="w-full space-y-4">
<button class="flex h-14 w-full items-center justify-center rounded-lg apple-button text-lg font-semibold">
<svg class="w-5 h-5 mr-3" fill="currentColor" viewBox="0 0 24 24"><path d="M19.33 12.06c0-3.3-2.07-4.97-4.83-4.97-2.31 0-4.14 1.5-5.23 1.5-.95 0-2.93-1.47-4.63-1.47-3.14 0-5.3 2.15-5.3 5.43 0 4.3 3.1 8.2 5.4 8.2 1.45 0 2.53-.9 4.1-.9s2.5.9 4.13.9c2.47 0 5.36-3.73 5.36-8.72zm-8.23 6.68c-.68.02-1.3-.4-1.88-.4-.9 0-1.9.46-2.6.46-.86 0-1.93-1.2-1.93-2.83 0-1.45 1-2.7 2.03-2.7.92 0 1.83.5 2.53.5.73 0 1.76-.53 2.53-.53.4 0 .73.18.98.42-.1.08-1.5 1.03-1.5 2.6s1.2 2.37 1.25 2.4zM16.3 7.85c.8-.93 1.28-2.2 1.1-3.5-.9.08-2.15.63-2.95 1.55-.7.83-1.37 2.1-1.2 3.32.98.12 2.18-.5 2.05-1.37z"></path></svg>
                Sign in with Apple
            </button>
<button class="flex h-14 w-full items-center justify-center rounded-lg google-button text-lg font-semibold">
<svg class="w-6 h-6 mr-3" viewBox="0 0 48 48"><path d="M43.611 20.083H42V20H24v8h11.303c-1.649 4.657-6.08 8-11.303 8c-6.627 0-12-5.373-12-12s5.373-12 12-12c3.059 0 5.842 1.154 7.961 3.039l5.657-5.657C34.046 6.053 29.268 4 24 4C12.955 4 4 12.955 4 24s8.955 20 20 20s20-8.955 20-20c0-1.341-.138-2.65-.389-3.917z" fill="#FFC107"></path><path d="M6.306 14.691l6.571 4.819C14.655 15.108 18.961 12 24 12c3.059 0 5.842 1.154 7.961 3.039l5.657-5.657C34.046 6.053 29.268 4 24 4C16.318 4 9.656 8.337 6.306 14.691z" fill="#FF3D00"></path><path d="M24 44c5.166 0 9.86-1.977 13.409-5.192l-6.19-5.238C29.211 35.091 26.715 36 24 36c-5.223 0-9.657-3.657-11.303-8H6.306C9.656 39.663 16.318 44 24 44z" fill="#4CAF50"></path><path d="M43.611 20.083H42V20H24v8h11.303c-.792 2.237-2.231 4.166-4.087 5.571l6.19 5.238C42.012 35.158 44 30.013 44 24c0-1.341-.138-2.65-.389-3.917z" fill="#1976D2"></path></svg>
                Sign in with Google
            </button>
</div>
<div class="relative flex py-5 items-center w-full">
<div class="flex-grow border-t border-slate-300 dark:border-slate-700"></div>
<span class="flex-shrink mx-4 text-text-secondary-light dark:text-text-secondary-dark">or</span>
<div class="flex-grow border-t border-slate-300 dark:border-slate-700"></div>
</div>
<form class="w-full space-y-4">
<div>
<input class="w-full h-14 form-input text-lg" placeholder="Email address" required="" type="email"/>
</div>
<div>
<input class="w-full h-14 form-input text-lg" placeholder="Password" required="" type="password"/>
</div>
<button class="w-full h-14 bg-primary text-white font-bold rounded-lg text-lg" type="submit">Sign In</button>
</form>
<div class="text-center mt-6">
<a class="text-primary font-semibold hover:underline" href="#">Forgot Password?</a>
</div>
</main>
<footer class="p-6 text-center border-t border-slate-200 dark:border-slate-700">
<p class="text-text-secondary-light dark:text-text-secondary-dark">Don't have an account? <a class="font-bold text-primary hover:underline" href="#">Sign up</a></p>
</footer>
</div>

</body></html>

<!-- Select Apps to Block -->
<!DOCTYPE html>
<html class="dark" lang="en"><head>
<meta charset="utf-8"/>
<title>Stitch Design</title>
<link href="data:image/x-icon;base64," rel="icon" type="image/x-icon"/>
<link crossorigin="" href="https://fonts.gstatic.com/" rel="preconnect"/>
<link as="style" href="https://fonts.googleapis.com/css2?display=swap&amp;family=Inter:wght@400;500;700;900&amp;family=Noto+Sans:wght@400;500;700;900" onload="this.rel='stylesheet'" rel="stylesheet"/>
<script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
<script>
    tailwind.config = {
      darkMode: "class",
      theme: {
        extend: {
          colors: {
            "primary": "#1173d4",
            "background-light": "#f6f7f8",
            "background-dark": "#101922",
          },
          fontFamily: {
            "display": ["Inter"]
          },
          borderRadius: {
            "DEFAULT": "0.5rem",
            "lg": "1rem",
            "xl": "1.5rem",
            "full": "9999px"
          },
        },
      },
    }
  </script>
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined" rel="stylesheet"/>
<style>
    body {
      min-height: max(884px, 100dvh);
    }
  </style>
  </head>
<body class="bg-background-light dark:bg-background-dark font-display">
<div class="flex flex-col h-screen justify-between">
<div class="flex-grow overflow-y-auto">
<header class="sticky top-0 z-10 bg-background-light/80 dark:bg-background-dark/80 backdrop-blur-sm">
<div class="flex items-center p-4">
<button class="text-slate-800 dark:text-white">
<span class="material-symbols-outlined">arrow_back_ios_new</span>
</button>
<h1 class="flex-1 text-center text-lg font-bold text-slate-900 dark:text-white pr-6">Select Apps</h1>
</div>
<div class="px-4 pb-4">
<div class="relative">
<div class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
<span class="material-symbols-outlined text-slate-500 dark:text-slate-400">search</span>
</div>
<input class="w-full rounded-lg border-0 bg-slate-200 dark:bg-slate-800 py-3 pl-10 pr-4 text-slate-900 dark:text-white placeholder:text-slate-500 dark:placeholder:text-slate-400 focus:ring-2 focus:ring-primary focus:outline-none" placeholder="Search apps" type="search"/>
</div>
</div>
</header>
<main class="px-4">
<h2 class="text-lg font-bold text-slate-900 dark:text-white mb-2">All Apps (7)</h2>
<div class="space-y-2">
<div class="flex items-center justify-between p-2 rounded-lg hover:bg-slate-200/50 dark:hover:bg-slate-800/50">
<div class="flex items-center gap-4">
<div class="size-12 rounded-lg bg-cover bg-center" style='background-image: url("https://lh3.googleusercontent.com/aida-public/AB6AXuBqc1ASbz6Bk2Uqv2PgCuGgRPmkEYzJFid_ZMGUVgTmJEf6nOJiVEg07UxYSxDcFyxopgFk_6IyjvDIFdCn6tacpq3acwNh253jvgCQpj1txxexJcrmsx2N3sOy1gm90eHPyhHF0yw7z2JI7EXxf9giN3jJojaPLIdI5iu7jqEqNNhRRxK_PnWsHoYfIXhS8Z-CJ1JD3uYqQy6Ax2TbuAlapV0dOm9ORWV9F9suJRy5KEi7uQfu7_U9aKX2jjaJAZ3G68N28ialzbE");'></div>
<div>
<p class="font-medium text-slate-900 dark:text-white">Chirp</p>
<p class="text-sm text-slate-500 dark:text-slate-400">Social</p>
</div>
</div>
<input class="h-6 w-6 rounded-md border-slate-300 dark:border-slate-600 bg-transparent text-primary focus:ring-primary focus:ring-offset-background-light dark:focus:ring-offset-background-dark" type="checkbox"/>
</div>
<div class="flex items-center justify-between p-2 rounded-lg hover:bg-slate-200/50 dark:hover:bg-slate-800/50">
<div class="flex items-center gap-4">
<div class="size-12 rounded-lg bg-cover bg-center" style='background-image: url("https://lh3.googleusercontent.com/aida-public/AB6AXuC5BQRpaoYl3f89G7TK2BIweqm2gh4TJrrA-ZT9ajUoX_zHinS8oBvY11gkknZIfki9SQIisAsFFrTeqBJDp6JQzsFyuKWtsYDy2zX8-piZpmIoQbCd0vuolXrjOX4QvERMZTfdz_jEjDnN-XljWvNXnC9NjD5TJt_Z1YJIfBqhNz-m1wV64ZJExjhV257jn_yN4AjGIQZUrxWMZirt1S6v3u5UEI6W9nNonP-EHIod8vqOMwgb3xadecpPl19IwuiFudOxZRXreEc");'></div>
<div>
<p class="font-medium text-slate-900 dark:text-white">FaceTime</p>
<p class="text-sm text-slate-500 dark:text-slate-400">Social</p>
</div>
</div>
<input checked="" class="h-6 w-6 rounded-md border-slate-300 dark:border-slate-600 bg-transparent text-primary focus:ring-primary focus:ring-offset-background-light dark:focus:ring-offset-background-dark" type="checkbox"/>
</div>
<div class="flex items-center justify-between p-2 rounded-lg hover:bg-slate-200/50 dark:hover:bg-slate-800/50">
<div class="flex items-center gap-4">
<div class="size-12 rounded-lg bg-cover bg-center" style='background-image: url("https://lh3.googleusercontent.com/aida-public/AB6AXuAMUAv9Frgh-JJeTSrno8g7xWtM9dsVw3301PH1dkuqO2Lh02ZMqsVgcYq-ugafXJCHFl5G_REAtPbDbeUUTf-6lwlDb3_4cfxwDwn62QrGFrxhoCOl-zRrdcdjq6iwp8LnbaId0tscfpRnWwJYaGpxtW8iNU9k5s3NmyQLtewGwvkpg7oIpa4yGZv2WOP374dse6Y3QqSQ1te2MA3njLkAP6NeC2n_n7NW3WauP5LBMDV6XnoNkkJwoSyG8iTy3MlZ4P24kcCwWco");'></div>
<div>
<p class="font-medium text-slate-900 dark:text-white">Insta</p>
<p class="text-sm text-slate-500 dark:text-slate-400">Social</p>
</div>
</div>
<input class="h-6 w-6 rounded-md border-slate-300 dark:border-slate-600 bg-transparent text-primary focus:ring-primary focus:ring-offset-background-light dark:focus:ring-offset-background-dark" type="checkbox"/>
</div>
<div class="flex items-center justify-between p-2 rounded-lg hover:bg-slate-200/50 dark:hover:bg-slate-800/50">
<div class="flex items-center gap-4">
<div class="size-12 rounded-lg bg-cover bg-center" style='background-image: url("https://lh3.googleusercontent.com/aida-public/AB6AXuAXsBQ6gZNWp9L1FJC4udzagqFtzzCpB4t0si02h6ODNWG_UWi3_F9a3_3BEdEQA2QyS1T8V6WzUjRz7G-XB2oifTfOkgFduHxFhkUsmXdQO_e6ELOxTEZ22O7TLPmIiYR1jqn_TdpL5AIzpU_qiSGDjzcNr_E_Q80wbz_WOHoJnB1W536_LFIOZWEkNZ7Znh4dMBBWVgaUly61WBmBBtcY7ElRTi9JQ4TxWRbvm4BllHsJguXVaGfOc3xzSjaa2Sx679JNDqujeDo");'></div>
<div>
<p class="font-medium text-slate-900 dark:text-white">Snap</p>
<p class="text-sm text-slate-500 dark:text-slate-400">Social</p>
</div>
</div>
<input class="h-6 w-6 rounded-md border-slate-300 dark:border-slate-600 bg-transparent text-primary focus:ring-primary focus:ring-offset-background-light dark:focus:ring-offset-background-dark" type="checkbox"/>
</div>
<div class="flex items-center justify-between p-2 rounded-lg hover:bg-slate-200/50 dark:hover:bg-slate-800/50">
<div class="flex items-center gap-4">
<div class="size-12 rounded-lg bg-cover bg-center" style='background-image: url("https://lh3.googleusercontent.com/aida-public/AB6AXuB0Wf-ubjmvx6k4ybjFcgAPqMu7JjapwDUtwGNKLqegqrMuyYrfPkbj08PXlEBQuA0VB8-dJUPIeBCmKzO_H-b76oSwhk4iw7cBbRuICI48GAU7TLMA524LelyPjvCmo2r3oJb7DqKLr7wyw1Q_ROsDxSRYhreO8fnugreKshb3jp9CDotYasmnbWy1o9i0Us9_PDQXk5bLIYO1ecDKNzIqbLt0OIq-tDXcdZqw1VoOg400cW4uhnJEvLHMzDHG_NukWkhwCI1TXqc");'></div>
<div>
<p class="font-medium text-slate-900 dark:text-white">TikTok</p>
<p class="text-sm text-slate-500 dark:text-slate-400">Social</p>
</div>
</div>
<input checked="" class="h-6 w-6 rounded-md border-slate-300 dark:border-slate-600 bg-transparent text-primary focus:ring-primary focus:ring-offset-background-light dark:focus:ring-offset-background-dark" type="checkbox"/>
</div>
<div class="flex items-center justify-between p-2 rounded-lg hover:bg-slate-200/50 dark:hover:bg-slate-800/50">
<div class="flex items-center gap-4">
<div class="size-12 rounded-lg bg-cover bg-center" style='background-image: url("https://lh3.googleusercontent.com/aida-public/AB6AXuD1A24j1JbBjcZBdbWGWTaFwQhpKCMadGv_vWqr5tPo67nfVNfxmP9OfFndfsiQuS5acWjDqDvkVEq8fbBWbI7S9rpPZlmFRmFc4Q0SqHHmu86q9nrP6TYU8o7u1qq4x0uR4jSTWs2QBF36yCQYcqClGsvdNaE-wBPI5bX3jXqIYWgqhfBHDeMRwtsoZyalk7h9f-moXikn7I9Ys_0GM-RcyqTFgVWF28BCLHwgcX0wt6OkR61ukTz2B80Dbr6tvHcO1TdIryt1ydc");'></div>
<div>
<p class="font-medium text-slate-900 dark:text-white">X</p>
<p class="text-sm text-slate-500 dark:text-slate-400">Social</p>
</div>
</div>
<input class="h-6 w-6 rounded-md border-slate-300 dark:border-slate-600 bg-transparent text-primary focus:ring-primary focus:ring-offset-background-light dark:focus:ring-offset-background-dark" type="checkbox"/>
</div>
<div class="flex items-center justify-between p-2 rounded-lg hover:bg-slate-200/50 dark:hover:bg-slate-800/50">
<div class="flex items-center gap-4">
<div class="size-12 rounded-lg bg-cover bg-center" style='background-image: url("https://lh3.googleusercontent.com/aida-public/AB6AXuDS156SYAKerSoSd0sYOgWbf_J3Afc1U-72iunTYq2K99iWjOO1Vr1gik833OrPG5MXuUbt4M4g5mL_36viYTF7yAJfT0OKeLSHDg53CjvJ095k7f5Y-PBu9NcFdPAs3ruUNU-KvtzXtbgTtHhBwrP2OWTr5VV_M59SPZp1kNlF2MUNGaiNDLbo0AScGnMLvLulkW0I0qejcGX-tDuu4uU6hxaiQu2GAHkDkJ_AsuumILBEQP9BJtCFmzaiCchoOCb0lKJfnYMlajY");'></div>
<div>
<p class="font-medium text-slate-900 dark:text-white">YouTube</p>
<p class="text-sm text-slate-500 dark:text-slate-400">Social</p>
</div>
</div>
<input class="h-6 w-6 rounded-md border-slate-300 dark:border-slate-600 bg-transparent text-primary focus:ring-primary focus:ring-offset-background-light dark:focus:ring-offset-background-dark" type="checkbox"/>
</div>
</div>
</main>
</div>
<footer class="bg-background-light dark:bg-background-dark p-4 border-t border-slate-200 dark:border-slate-800">
<div class="flex gap-4">
<button class="w-full h-12 rounded-lg bg-primary/10 text-primary dark:bg-primary/20 dark:text-primary font-bold text-sm">Deselect All</button>
<button class="w-full h-12 rounded-lg bg-primary text-white font-bold text-sm">Select All</button>
</div>
</footer>
</div>

</body></html>

<!-- Usage Statistics -->
<!DOCTYPE html>
<html class="dark" lang="en"><head>
<meta charset="utf-8"/>
<meta content="width=device-width, initial-scale=1.0" name="viewport"/>
<title>Stitch Design</title>
<link href="data:image/x-icon;base64," rel="icon" type="image/x-icon"/>
<link href="https://fonts.googleapis.com" rel="preconnect"/>
<link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect"/>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&amp;display=swap" rel="stylesheet"/>
<script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
<script id="tailwind-config">
        tailwind.config = {
            darkMode: "class",
            theme: {
                extend: {
                    colors: {
                        "primary": "#1173d4",
                        "background-light": "#f6f7f8",
                        "background-dark": "#101922",
                    },
                    fontFamily: {
                        "display": ["Inter"]
                    },
                    borderRadius: {
                        "DEFAULT": "0.5rem",
                        "lg": "1rem",
                        "xl": "1.5rem",
                        "full": "9999px"
                    },
                },
            },
        }
    </script>
<style>
        body {
            min-height: max(884px, 100dvh);
        }
    </style>
<style>
    body {
      min-height: max(884px, 100dvh);
    }
  </style>
  </head>
<body class="bg-background-light dark:bg-background-dark font-display">
<div class="flex flex-col min-h-screen">
<header class="sticky top-0 z-10 bg-background-light/80 dark:bg-background-dark/80 backdrop-blur-sm">
<div class="flex items-center p-4">
<button class="p-2 text-slate-800 dark:text-white">
<svg fill="currentColor" height="24" viewBox="0 0 256 256" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="M224,128a8,8,0,0,1-8,8H59.31l58.35,58.34a8,8,0,0,1-11.32,11.32l-72-72a8,8,0,0,1,0-11.32l72-72a8,8,0,0,1,11.32,11.32L59.31,120H216A8,8,0,0,1,224,128Z"></path>
</svg>
</button>
<h1 class="text-lg font-bold text-slate-900 dark:text-white text-center flex-1 pr-10">Settings</h1>
</div>
</header>
<main class="flex-1 overflow-y-auto p-4 space-y-6">
<div class="space-y-6">
<div>
<h2 class="text-xs font-bold uppercase text-slate-500 dark:text-slate-400 px-4 mb-2">Subscription</h2>
<div class="bg-slate-100 dark:bg-slate-800/60 rounded-xl p-4">
<div class="flex items-center justify-between mb-2">
<div>
<p class="text-base font-semibold text-slate-800 dark:text-white">Premium Plan</p>
<p class="text-sm text-slate-500 dark:text-slate-400">Renews in 25 days</p>
</div>
<div class="bg-green-100 dark:bg-green-900/50 text-green-700 dark:text-green-300 text-xs font-bold px-2.5 py-1 rounded-full">
                                ACTIVE
                            </div>
</div>
<a class="w-full text-center bg-primary text-white font-semibold py-2.5 rounded-lg block mt-4 hover:bg-primary/90 transition-colors" href="#">Manage Subscription</a>
</div>
</div>
<div>
<h2 class="text-xs font-bold uppercase text-slate-500 dark:text-slate-400 px-4 mb-2">General</h2>
<div class="bg-slate-100 dark:bg-slate-800/60 rounded-xl">
<div class="flex items-center justify-between p-4 border-b border-slate-200 dark:border-slate-700/60">
<div class="flex items-center gap-4">
<svg class="text-slate-500 dark:text-slate-400" fill="none" height="24" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"></path>
</svg>
<span class="font-medium text-slate-800 dark:text-white">Dark Mode</span>
</div>
<label class="relative inline-flex items-center cursor-pointer">
<input checked="" class="sr-only peer" type="checkbox"/>
<div class="w-11 h-6 bg-slate-200 dark:bg-slate-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-slate-600 peer-checked:bg-primary"></div>
</label>
</div>
<a class="flex items-center justify-between p-4" href="#">
<div class="flex items-center gap-4">
<svg class="text-slate-500 dark:text-slate-400" fill="none" height="24" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="m12 14 4-4"></path>
<path d="M3.34 19a10 10 0 1 1 17.32 0"></path>
</svg>
<span class="font-medium text-slate-800 dark:text-white">Manage Blocked Apps</span>
</div>
<svg class="text-slate-400 dark:text-slate-500" fill="currentColor" height="20" viewBox="0 0 256 256" width="20" xmlns="http://www.w3.org/2000/svg">
<path d="M181.66,133.66l-80,80a8,8,0,0,1-11.32-11.32L164.69,128,90.34,53.66a8,8,0,0,1,11.32-11.32l80,80A8,8,0,0,1,181.66,133.66Z"></path>
</svg>
</a>
</div>
</div>
<div>
<h2 class="text-xs font-bold uppercase text-slate-500 dark:text-slate-400 px-4 mb-2">Notifications</h2>
<div class="bg-slate-100 dark:bg-slate-800/60 rounded-xl">
<div class="flex items-center justify-between p-4 border-b border-slate-200 dark:border-slate-700/60">
<div class="flex items-center gap-4">
<svg class="text-slate-500 dark:text-slate-400" fill="none" height="24" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"></path>
<path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"></path>
</svg>
<span class="font-medium text-slate-800 dark:text-white">Focus Session Alerts</span>
</div>
<label class="relative inline-flex items-center cursor-pointer">
<input class="sr-only peer" type="checkbox"/>
<div class="w-11 h-6 bg-slate-200 dark:bg-slate-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-slate-600 peer-checked:bg-primary"></div>
</label>
</div>
<div class="flex items-center justify-between p-4">
<div class="flex items-center gap-4">
<svg class="text-slate-500 dark:text-slate-400" fill="none" height="24" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="m17 2 4 4-4 4"></path>
<path d="M3 11v-1a4 4 0 0 1 4-4h14"></path>
<path d="m7 22-4-4 4-4"></path>
<path d="M21 13v1a4 4 0 0 1-4 4H3"></path>
</svg>
<span class="font-medium text-slate-800 dark:text-white">Usage Reminders</span>
</div>
<label class="relative inline-flex items-center cursor-pointer">
<input checked="" class="sr-only peer" type="checkbox"/>
<div class="w-11 h-6 bg-slate-200 dark:bg-slate-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-slate-600 peer-checked:bg-primary"></div>
</label>
</div>
</div>
</div>
<div>
<h2 class="text-xs font-bold uppercase text-slate-500 dark:text-slate-400 px-4 mb-2">Data &amp; Privacy</h2>
<div class="bg-slate-100 dark:bg-slate-800/60 rounded-xl">
<a class="flex items-center justify-between p-4 border-b border-slate-200 dark:border-slate-700/60" href="#">
<div class="flex items-center gap-4">
<svg class="text-slate-500 dark:text-slate-400" fill="none" height="24" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10"></path>
<path d="m9 12 2 2 4-4"></path>
</svg>
<span class="font-medium text-slate-800 dark:text-white">Privacy Policy</span>
</div>
<svg class="text-slate-400 dark:text-slate-500" fill="currentColor" height="20" viewBox="0 0 256 256" width="20" xmlns="http://www.w3.org/2000/svg">
<path d="M181.66,133.66l-80,80a8,8,0,0,1-11.32-11.32L164.69,128,90.34,53.66a8,8,0,0,1,11.32-11.32l80,80A8,8,0,0,1,181.66,133.66Z"></path>
</svg>
</a>
<a class="flex items-center justify-between p-4" href="#">
<div class="flex items-center gap-4">
<svg class="text-slate-500 dark:text-slate-400" fill="none" height="24" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="M12 21v-4"></path>
<path d="M16 17H8"></path>
<path d="M5 21a7 7 0 0 1 14 0M12 3v10"></path>
<path d="M10 5.5 12 3l2 2.5"></path>
<path d="m14 11-2 2-2-2"></path>
</svg>
<span class="font-medium text-slate-800 dark:text-white">Export My Data</span>
</div>
<svg class="text-slate-400 dark:text-slate-500" fill="currentColor" height="20" viewBox="0 0 256 256" width="20" xmlns="http://www.w3.org/2000/svg">
<path d="M181.66,133.66l-80,80a8,8,0,0,1-11.32-11.32L164.69,128,90.34,53.66a8,8,0,0,1,11.32-11.32l80,80A8,8,0,0,1,181.66,133.66Z"></path>
</svg>
</a>
</div>
</div>
<div>
<h2 class="text-xs font-bold uppercase text-slate-500 dark:text-slate-400 px-4 mb-2">Support</h2>
<div class="bg-slate-100 dark:bg-slate-800/60 rounded-xl">
<a class="flex items-center justify-between p-4 border-b border-slate-200 dark:border-slate-700/60" href="#">
<div class="flex items-center gap-4">
<svg class="text-slate-500 dark:text-slate-400" fill="none" height="24" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="M9.1 9a3 3 0 0 1 5.82 1c0 2-3 3-3 3"></path>
<path d="M12 17h.01"></path>
<circle cx="12" cy="12" r="10"></circle>
</svg>
<span class="font-medium text-slate-800 dark:text-white">Help Center</span>
</div>
<svg class="text-slate-400 dark:text-slate-500" fill="currentColor" height="20" viewBox="0 0 256 256" width="20" xmlns="http://www.w3.org/2000/svg">
<path d="M181.66,133.66l-80,80a8,8,0,0,1-11.32-11.32L164.69,128,90.34,53.66a8,8,0,0,1,11.32-11.32l80,80A8,8,0,0,1,181.66,133.66Z"></path>
</svg>
</a>
<a class="flex items-center justify-between p-4" href="#">
<div class="flex items-center gap-4">
<svg class="text-slate-500 dark:text-slate-400" fill="none" height="24" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
<circle cx="12" cy="12" r="10"></circle>
<path d="M12 16v-4"></path>
<path d="M12 8h.01"></path>
</svg>
<span class="font-medium text-slate-800 dark:text-white">About</span>
</div>
<svg class="text-slate-400 dark:text-slate-500" fill="currentColor" height="20" viewBox="0 0 256 256" width="20" xmlns="http://www.w3.org/2000/svg">
<path d="M181.66,133.66l-80,80a8,8,0,0,1-11.32-11.32L164.69,128,90.34,53.66a8,8,0,0,1,11.32-11.32l80,80A8,8,0,0,1,181.66,133.66Z"></path>
</svg>
</a>
</div>
</div>
</div>
</main>
<footer class="sticky bottom-0 bg-background-light/80 dark:bg-background-dark/80 backdrop-blur-sm border-t border-slate-200 dark:border-slate-800">
<nav class="flex justify-around items-center h-16">
<a class="flex flex-col items-center gap-1 text-slate-500 dark:text-slate-400" href="#">
<svg fill="currentColor" height="24" viewBox="0 0 256 256" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="M221.87,83.16A104.1,104.1,0,1,1,195.67,49l22.67-22.68a8,8,0,0,1,11.32,11.32l-96,96a8,8,0,0,1-11.32-11.32l27.72-27.72a40,40,0,1,0,17.87,31.09,8,8,0,1,1,16-.9,56,56,0,1,1-22.38-41.65L184.3,60.39a87.88,87.88,0,1,0,23.13,29.67,8,8,0,0,1,14.44-6.9Z"></path>
</svg>
<span class="text-xs font-medium">Focus</span>
</a>
<a class="flex flex-col items-center gap-1 text-slate-500 dark:text-slate-400" href="#">
<svg fill="currentColor" height="24" viewBox="0 0 256 256" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="M216,40H40A16,16,0,0,0,24,56V200a16,16,0,0,0,16,16H216a16,16,0,0,0,16-16V56A16,16,0,0,0,216,40ZM200,176a8,8,0,0,1,0,16H56a8,8,0,0,1-8-8V72a8,8,0,0,1,16,0v62.92l34.88-29.07a8,8,0,0,1,9.56-.51l43,28.69,43.41-36.18a8,8,0,0,1,10.24,12.3l-48,40a8,8,0,0,1-9.56.51l-43-28.69L64,155.75V176Z"></path>
</svg>
<span class="text-xs font-medium">Statistics</span>
</a>
<a class="flex flex-col items-center gap-1 text-primary" href="#">
<svg fill="currentColor" height="24" viewBox="0 0 256 256" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="M128,80a48,48,0,1,0,48,48A48.05,48.05,0,0,0,128,80Zm0,80a32,32,0,1,1,32-32A32,32,0,0,1,128,160Zm88-29.84q.06-2.16,0-4.32l14.92-18.64a8,8,0,0,0,1.48-7.06,107.21,107.21,0,0,0-10.88-26.25,8,8,0,0,0-6-3.93l-23.72-2.64q-1.48-1.56-3-3L186,40.54a8,8,0,0,0-3.94-6,107.71,107.71,0,0,0-26.25-10.87,8,8,0,0,0-7.06,1.49L130.16,40Q128,40,125.84,40L107.2,25.11a8,8,0,0,0-7.06-1.48A107.6,107.6,0,0,0,73.89,34.51a8,8,0,0,0-3.93,6L67.32,64.27q-1.56,1.49-3,3L40.54,70a8,8,0,0,0-6,3.94,107.71,107.71,0,0,0-10.87,26.25,8,8,0,0,0,1.49,7.06L40,125.84Q40,128,40,130.16L25.11,148.8a8,8,0,0,0-1.48,7.06,107.21,107.21,0,0,0,10.88,26.25,8,8,0,0,0,6,3.93l23.72,2.64q1.49,1.56,3,3L70,215.46a8,8,0,0,0,3.94,6,107.71,107.71,0,0,0,26.25,10.87,8,8,0,0,0,7.06-1.49L125.84,216q2.16.06,4.32,0l18.64,14.92a8,8,0,0,0,7.06,1.48,107.21,107.21,0,0,0,26.25-10.88,8,8,0,0,0,3.93-6l2.64-23.72q1.56-1.48,3-3L215.46,186a8,8,0,0,0,6-3.94,107.71,107.71,0,0,0,10.87-26.25,8,8,0,0,0-1.49-7.06Zm-16.1-6.5a73.93,73.93,0,0,1,0,8.68,8,8,0,0,0,1.74,5.48l14.19,17.73a91.57,91.57,0,0,1-6.23,15L187,173.11a8,8,0,0,0-5.1,2.64,74.11,74.11,0,0,1-6.14,6.14,8,8,0,0,0-2.64,5.1l-2.51,22.58a91.32,91.32,0,0,1-15,6.23l-17.74-14.19a8,8,0,0,0-5-1.75h-.48a73.93,73.93,0,0,1-8.68,0,8,8,0,0,0-5.48,1.74L100.45,215.8a91.57,91.57,0,0,1-15-6.23L82.89,187a8,8,0,0,0-2.64-5.1,74.11,74.11,0,0,1-6.14-6.14,8,8,0,0,0-5.1-2.64L46.43,170.6a91.32,91.32,0,0,1-6.23-15l14.19-17.74a8,8,0,0,0,1.74-5.48,73.93,73.93,0,0,1,0-8.68,8,8,0,0,0-1.74-5.48L40.2,100.45a91.57,91.57,0,0,1,6.23-15L69,82.89a8,8,0,0,0,5.1-2.64,74.11,74.11,0,0,1,6.14-6.14A8,8,0,0,0,82.89,69L85.4,46.43a91.32,91.32,0,0,1,15-6.23l17.74,14.19a8,8,0,0,0,5.48,1.74,73.93,73.93,0,0,1,8.68,0,8,8,0,0,0,5.48-1.74L155.55,40.2a91.57,91.57,0,0,1,15,6.23L173.11,69a8,8,0,0,0,2.64,5.1,74.11,74.11,0,0,1,6.14,6.14,8,8,0,0,0,5.1,2.64l22.58,2.51a91.32,91.32,0,0,1,6.23,15l-14.19,17.74A8,8,0,0,0,199.87,123.66Z"></path>
</svg>
<span class="text-xs font-medium">Settings</span>
</a>
</nav>
<div class="h-safe-bottom"></div>
</footer>
</div>

</body></html>

<!-- Trial & Subscription Prompt -->
<!DOCTYPE html>
<html class="dark" lang="en"><head>
<meta charset="utf-8"/>
<title>Digital Wellness App</title>
<link crossorigin="" href="https://fonts.gstatic.com/" rel="preconnect"/>
<link as="style" href="https://fonts.googleapis.com/css2?display=swap&amp;family=Inter:wght@400;500;700;900&amp;family=Noto+Sans:wght@400;500;700;900" onload="this.rel='stylesheet'" rel="stylesheet"/>
<script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
<script>
    tailwind.config = {
      darkMode: "class",
      theme: {
        extend: {
          colors: {
            "primary": "#1173d4",
            "background-light": "#f6f7f8",
            "background-dark": "#101922",
          },
          fontFamily: {
            "display": ["Inter", "Noto Sans", "sans-serif"]
          },
          borderRadius: {
            "DEFAULT": "0.5rem",
            "lg": "1rem",
            "xl": "1.5rem",
            "full": "9999px"
          },
        },
      },
    }
  </script>
<style>
    body {
      min-height: max(884px, 100dvh);
    }
  </style>
  </head>
<body class="bg-background-light dark:bg-background-dark font-display">
<div class="relative flex h-screen w-full flex-col justify-end bg-black/30 dark:bg-black/50">
<div class="flex w-full flex-col rounded-t-xl bg-background-light dark:bg-background-dark">
<div class="flex w-full items-center justify-center py-2">
<div class="h-1.5 w-10 rounded-full bg-slate-300 dark:bg-slate-600"></div>
</div>
<div class="flex flex-col gap-2 px-4 pb-6 pt-4 text-center">
<h1 class="text-2xl font-bold text-slate-800 dark:text-white">Unlock all features with Premium</h1>
<p class="text-base font-normal text-slate-600 dark:text-slate-300">Get unlimited access to all features and continue your journey to a more focused you.</p>
</div>
<div class="flex flex-col gap-3 px-4 pb-6">
<button class="flex h-12 w-full items-center justify-center rounded-lg bg-primary px-5 text-base font-bold text-white">Start 7-day free trial</button>
<button class="flex h-12 w-full items-center justify-center rounded-lg bg-primary/10 px-5 text-base font-bold text-primary dark:bg-primary/20">Subscribe now</button>
<button class="flex h-10 w-full items-center justify-center rounded-lg bg-transparent px-4 text-sm font-bold text-slate-500 dark:text-slate-400">Maybe later</button>
</div>
</div>
</div>

</body></html>

<!-- Usage Statistics -->
<!DOCTYPE html>
<html class="dark" lang="en"><head>
<meta charset="utf-8"/>
<title>Stitch Design</title>
<link href="data:image/x-icon;base64," rel="icon" type="image/x-icon"/>
<link href="https://fonts.googleapis.com" rel="preconnect"/>
<link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect"/>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&amp;display=swap" rel="stylesheet"/>
<script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
<script id="tailwind-config">
    tailwind.config = {
      darkMode: "class",
      theme: {
        extend: {
          colors: {
            "primary": "#1173d4",
            "background-light": "#f6f7f8",
            "background-dark": "#101922",
          },
          fontFamily: {
            "display": ["Inter"]
          },
          borderRadius: {
            "DEFAULT": "0.5rem",
            "lg": "1rem",
            "xl": "1.5rem",
            "full": "9999px"
          },
        },
      },
    }
  </script>
<style>
    body {
      min-height: max(884px, 100dvh);
    }
  </style>
<style>
    body {
      min-height: max(884px, 100dvh);
    }
  </style>
  </head>
<body class="bg-background-light dark:bg-background-dark font-display">
<div class="flex flex-col min-h-screen">
<header class="sticky top-0 z-10 bg-background-light/80 dark:bg-background-dark/80 backdrop-blur-sm">
<div class="flex items-center p-4">
<button class="p-2 text-slate-800 dark:text-white">
<svg fill="currentColor" height="24" viewBox="0 0 256 256" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="M224,128a8,8,0,0,1-8,8H59.31l58.35,58.34a8,8,0,0,1-11.32,11.32l-72-72a8,8,0,0,1,0-11.32l72-72a8,8,0,0,1,11.32,11.32L59.31,120H216A8,8,0,0,1,224,128Z"></path>
</svg>
</button>
<h1 class="text-lg font-bold text-slate-900 dark:text-white text-center flex-1 pr-10">Usage Statistics</h1>
</div>
<div class="px-4 pb-4">
<div class="flex rounded-lg bg-slate-200 dark:bg-slate-800/60 p-1">
<label class="flex-1">
<input checked="" class="sr-only" name="time-period" type="radio" value="Day"/>
<span class="block text-center text-sm font-semibold p-2 rounded-[0.4rem] cursor-pointer text-slate-600 dark:text-slate-400 peer-checked:bg-primary peer-checked:text-white transition-colors">Day</span>
</label>
<label class="flex-1">
<input class="sr-only" name="time-period" type="radio" value="Week"/>
<span class="block text-center text-sm font-semibold p-2 rounded-[0.4rem] cursor-pointer text-slate-600 dark:text-slate-400 peer-checked:bg-primary peer-checked:text-white transition-colors">Week</span>
</label>
<label class="flex-1">
<input class="sr-only" name="time-period" type="radio" value="Month"/>
<span class="block text-center text-sm font-semibold p-2 rounded-[0.4rem] cursor-pointer text-slate-600 dark:text-slate-400 peer-checked:bg-primary peer-checked:text-white transition-colors">Month</span>
</label>
</div>
</div>
</header>
<main class="flex-1 overflow-y-auto p-4 space-y-6">
<div class="rounded-xl bg-slate-100 dark:bg-slate-800/60 p-4">
<h2 class="text-base font-semibold text-slate-600 dark:text-slate-300">App Usage Duration</h2>
<div class="space-y-4 mt-4">
<div class="space-y-2">
<div class="flex justify-between items-center">
<span class="text-sm font-medium text-slate-800 dark:text-white">Productivity App</span>
<span class="text-sm font-medium text-slate-500 dark:text-slate-400">1h 15m</span>
</div>
<div class="w-full bg-slate-200 dark:bg-slate-700 rounded-full h-2">
<div class="bg-primary h-2 rounded-full" style="width: 75%"></div>
</div>
<p class="text-xs text-slate-500 dark:text-slate-400">Opened 5 times</p>
</div>
<div class="space-y-2">
<div class="flex justify-between items-center">
<span class="text-sm font-medium text-slate-800 dark:text-white">Social Media</span>
<span class="text-sm font-medium text-slate-500 dark:text-slate-400">45m</span>
</div>
<div class="w-full bg-slate-200 dark:bg-slate-700 rounded-full h-2">
<div class="bg-primary h-2 rounded-full" style="width: 45%"></div>
</div>
<p class="text-xs text-slate-500 dark:text-slate-400">Opened 12 times</p>
</div>
<div class="space-y-2">
<div class="flex justify-between items-center">
<span class="text-sm font-medium text-slate-800 dark:text-white">Messaging App</span>
<span class="text-sm font-medium text-slate-500 dark:text-slate-400">30m</span>
</div>
<div class="w-full bg-slate-200 dark:bg-slate-700 rounded-full h-2">
<div class="bg-primary h-2 rounded-full" style="width: 30%"></div>
</div>
<p class="text-xs text-slate-500 dark:text-slate-400">Opened 8 times</p>
</div>
</div>
</div>
<div>
<h3 class="text-lg font-bold text-slate-900 dark:text-white mb-2">Today's App Usage</h3>
<div class="space-y-2">
<div class="p-3 rounded-lg bg-slate-100 dark:bg-slate-800/60">
<div class="flex items-center gap-4">
<div class="flex items-center justify-center rounded-lg bg-slate-200 dark:bg-slate-700/80 size-12 text-slate-600 dark:text-slate-300">
<svg class="feather feather-briefcase" fill="none" height="24" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg"><rect height="14" rx="2" ry="2" width="20" x="2" y="7"></rect><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path></svg>
</div>
<div class="flex-1">
<div class="flex justify-between items-center">
<p class="font-semibold text-slate-800 dark:text-white">Productivity App</p>
<p class="text-sm text-slate-500 dark:text-slate-400">10:00 AM - 10:45 AM</p>
</div>
<p class="text-sm text-slate-500 dark:text-slate-400">Duration: 45m</p>
</div>
</div>
</div>
<div class="p-3 rounded-lg bg-slate-100 dark:bg-slate-800/60">
<div class="flex items-center gap-4">
<div class="flex items-center justify-center rounded-lg bg-slate-200 dark:bg-slate-700/80 size-12 text-slate-600 dark:text-slate-300">
<svg fill="currentColor" height="24" viewBox="0 0 256 256" width="24" xmlns="http://www.w3.org/2000/svg"><path d="M128,80a48,48,0,1,0,48,48A48.05,48.05,0,0,0,128,80Zm0,80a32,32,0,1,1,32-32A32,32,0,0,1,128,160ZM176,24H80A56.06,56.06,0,0,0,24,80v96a56.06,56.06,0,0,0,56,56h96a56.06,56.06,0,0,0,56-56V80A56.06,56.06,0,0,0,176,24Zm40,152a40,40,0,0,1-40,40H80a40,40,0,0,1-40-40V80A40,40,0,0,1,80,40h96a40,40,0,0,1,40,40ZM192,76a12,12,0,1,1-12-12A12,12,0,0,1,192,76Z"></path></svg>
</div>
<div class="flex-1">
<div class="flex justify-between items-center">
<p class="font-semibold text-slate-800 dark:text-white">Social Media</p>
<p class="text-sm text-slate-500 dark:text-slate-400">11:30 AM - 11:40 AM</p>
</div>
<p class="text-sm text-slate-500 dark:text-slate-400">Duration: 10m</p>
<div class="mt-1 p-2 rounded-md bg-amber-100 dark:bg-amber-900/50">
<p class="text-xs text-amber-700 dark:text-amber-300"><span class="font-semibold">Override:</span> Urgent work communication</p>
</div>
</div>
</div>
</div>
<div class="p-3 rounded-lg bg-slate-100 dark:bg-slate-800/60">
<div class="flex items-center gap-4">
<div class="flex items-center justify-center rounded-lg bg-slate-200 dark:bg-slate-700/80 size-12 text-slate-600 dark:text-slate-300">
<svg class="feather feather-message-square" fill="none" height="24" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
</div>
<div class="flex-1">
<div class="flex justify-between items-center">
<p class="font-semibold text-slate-800 dark:text-white">Messaging App</p>
<p class="text-sm text-slate-500 dark:text-slate-400">12:45 PM - 12:55 PM</p>
</div>
<p class="text-sm text-slate-500 dark:text-slate-400">Duration: 10m</p>
</div>
</div>
</div>
</div>
</div>
</main>
<footer class="sticky bottom-0 bg-background-light/80 dark:bg-background-dark/80 backdrop-blur-sm border-t border-slate-200 dark:border-slate-800">
<nav class="flex justify-around items-center h-16">
<a class="flex flex-col items-center gap-1 text-slate-500 dark:text-slate-400" href="#">
<svg fill="currentColor" height="24" viewBox="0 0 256 256" width="24" xmlns="http://www.w3.org/2000/svg"><path d="M221.87,83.16A104.1,104.1,0,1,1,195.67,49l22.67-22.68a8,8,0,0,1,11.32,11.32l-96,96a8,8,0,0,1-11.32-11.32l27.72-27.72a40,40,0,1,0,17.87,31.09,8,8,0,1,1,16-.9,56,56,0,1,1-22.38-41.65L184.3,60.39a87.88,87.88,0,1,0,23.13,29.67,8,8,0,0,1,14.44-6.9Z"></path></svg>
<span class="text-xs font-medium">Focus</span>
</a>
<a class="flex flex-col items-center gap-1 text-primary" href="#">
<svg fill="currentColor" height="24" viewBox="0 0 256 256" width="24" xmlns="http://www.w3.org/2000/svg"><path d="M216,40H40A16,16,0,0,0,24,56V200a16,16,0,0,0,16,16H216a16,16,0,0,0,16-16V56A16,16,0,0,0,216,40ZM200,176a8,8,0,0,1,0,16H56a8,8,0,0,1-8-8V72a8,8,0,0,1,16,0v62.92l34.88-29.07a8,8,0,0,1,9.56-.51l43,28.69,43.41-36.18a8,8,0,0,1,10.24,12.3l-48,40a8,8,0,0,1-9.56.51l-43-28.69L64,155.75V176Z"></path></svg>
<span class="text-xs font-medium">Statistics</span>
</a>
<a class="flex flex-col items-center gap-1 text-slate-500 dark:text-slate-400" href="#">
<svg fill="currentColor" height="24" viewBox="0 0 256 256" width="24" xmlns="http://www.w3.org/2000/svg"><path d="M128,80a48,48,0,1,0,48,48A48.05,48.05,0,0,0,128,80Zm0,80a32,32,0,1,1,32-32A32,32,0,0,1,128,160Zm88-29.84q.06-2.16,0-4.32l14.92-18.64a8,8,0,0,0,1.48-7.06,107.21,107.21,0,0,0-10.88-26.25,8,8,0,0,0-6-3.93l-23.72-2.64q-1.48-1.56-3-3L186,40.54a8,8,0,0,0-3.94-6,107.71,107.71,0,0,0-26.25-10.87,8,8,0,0,0-7.06,1.49L130.16,40Q128,40,125.84,40L107.2,25.11a8,8,0,0,0-7.06-1.48A107.6,107.6,0,0,0,73.89,34.51a8,8,0,0,0-3.93,6L67.32,64.27q-1.56,1.49-3,3L40.54,70a8,8,0,0,0-6,3.94,107.71,107.71,0,0,0-10.87,26.25,8,8,0,0,0,1.49,7.06L40,125.84Q40,128,40,130.16L25.11,148.8a8,8,0,0,0-1.48,7.06,107.21,107.21,0,0,0,10.88,26.25,8,8,0,0,0,6,3.93l23.72,2.64q1.49,1.56,3,3L70,215.46a8,8,0,0,0,3.94,6,107.71,107.71,0,0,0,26.25,10.87,8,8,0,0,0,7.06-1.49L125.84,216q2.16.06,4.32,0l18.64,14.92a8,8,0,0,0,7.06,1.48,107.21,107.21,0,0,0,26.25-10.88,8,8,0,0,0,3.93-6l2.64-23.72q1.56-1.48,3-3L215.46,186a8,8,0,0,0,6-3.94,107.71,107.71,0,0,0,10.87-26.25,8,8,0,0,0-1.49-7.06Zm-16.1-6.5a73.93,73.93,0,0,1,0,8.68,8,8,0,0,0,1.74,5.48l14.19,17.73a91.57,91.57,0,0,1-6.23,15L187,173.11a8,8,0,0,0-5.1,2.64,74.11,74.11,0,0,1-6.14,6.14,8,8,0,0,0-2.64,5.1l-2.51,22.58a91.32,91.32,0,0,1-15,6.23l-17.74-14.19a8,8,0,0,0-5-1.75h-.48a73.93,73.93,0,0,1-8.68,0,8,8,0,0,0-5.48,1.74L100.45,215.8a91.57,91.57,0,0,1-15-6.23L82.89,187a8,8,0,0,0-2.64-5.1,74.11,74.11,0,0,1-6.14-6.14,8,8,0,0,0-5.1-2.64L46.43,170.6a91.32,91.32,0,0,1-6.23-15l14.19-17.74a8,8,0,0,0,1.74-5.48,73.93,73.93,0,0,1,0-8.68,8,8,0,0,0-1.74-5.48L40.2,100.45a91.57,91.57,0,0,1,6.23-15L69,82.89a8,8,0,0,0,5.1-2.64,74.11,74.11,0,0,1,6.14-6.14A8,8,0,0,0,82.89,69L85.4,46.43a91.32,91.32,0,0,1,15-6.23l17.74,14.19a8,8,0,0,0,5.48,1.74,73.93,73.93,0,0,1,8.68,0,8,8,0,0,0,5.48-1.74L155.55,40.2a91.57,91.57,0,0,1,15,6.23L173.11,69a8,8,0,0,0,2.64,5.1,74.11,74.11,0,0,1,6.14,6.14,8,8,0,0,0,5.1,2.64l22.58,2.51a91.32,91.32,0,0,1,6.23,15l-14.19,17.74A8,8,0,0,0,199.87,123.66Z"></path></svg>
<span class="text-xs font-medium">Settings</span>
</a>
</nav>
<div class="h-safe-bottom"></div>
</footer>
</div>

</body></html>

<!-- Set Permissions -->
<!DOCTYPE html>
<html class="dark" lang="en"><head>
<meta charset="utf-8"/>
<meta content="width=device-width, initial-scale=1.0" name="viewport"/>
<link crossorigin="" href="https://fonts.gstatic.com/" rel="preconnect"/>
<link as="style" href="https://fonts.googleapis.com/css2?display=swap&amp;family=Inter%3Awght%40400%3B500%3B700%3B900&amp;family=Noto+Sans%3Awght%40400%3B500%3B700%3B900" onload="this.rel='stylesheet'" rel="stylesheet"/>
<script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
<script id="tailwind-config">
      tailwind.config = {
        darkMode: "class",
        theme: {
          extend: {
            colors: {
              "primary": "#1173d4",
              "background-light": "#f6f7f8",
              "background-dark": "#101922",
            },
            fontFamily: {
              "display": ["Inter"]
            },
            borderRadius: {
              "DEFAULT": "0.5rem",
              "lg": "1rem",
              "xl": "1.5rem",
              "full": "9999px"
            },
          },
        },
      }
    </script>
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined" rel="stylesheet"/>
<title>Permissions</title>
<style>
    body {
      min-height: max(884px, 100dvh);
    }
  </style>
  </head>
<body class="bg-background-light dark:bg-background-dark font-display">
<div class="flex flex-col h-screen justify-between">
<div class="p-6">
<div class="flex items-center mb-8">
<button class="text-slate-800 dark:text-white">
<span class="material-symbols-outlined">arrow_back</span>
</button>
<h1 class="flex-grow text-center text-lg font-bold text-slate-900 dark:text-white pr-6">Permissions</h1>
</div>
<h2 class="text-2xl font-bold text-slate-900 dark:text-white mb-2">Grant Permissions</h2>
<p class="text-slate-600 dark:text-slate-400 mb-8">
          To effectively manage your digital wellness, the app requires certain permissions. These permissions allow the app to monitor app usage and block distracting apps in real-time. Your privacy is our priority, and we ensure that your data is handled securely and responsibly.
        </p>
<div class="space-y-6">
<div class="rounded-lg bg-background-light dark:bg-background-dark border border-slate-200 dark:border-slate-800">
<div class="flex items-center gap-4 p-4">
<div class="bg-primary/10 text-primary p-3 rounded-lg flex items-center justify-center">
<span class="material-symbols-outlined">bar_chart</span>
</div>
<div class="flex-1">
<p class="font-semibold text-slate-900 dark:text-white">Usage Access</p>
<p class="text-sm text-slate-600 dark:text-slate-400">Allows the app to monitor which apps you're using and for how long.</p>
</div>
</div>
<div class="px-4 pb-4 flex justify-end">
<button class="bg-primary/10 text-primary text-sm font-bold py-2 px-4 rounded-md">Grant Access</button>
</div>
</div>
<div class="rounded-lg bg-background-light dark:bg-background-dark border border-slate-200 dark:border-slate-800">
<div class="flex items-center gap-4 p-4">
<div class="bg-primary/10 text-primary p-3 rounded-lg flex items-center justify-center">
<span class="material-symbols-outlined">accessibility_new</span>
</div>
<div class="flex-1">
<p class="font-semibold text-slate-900 dark:text-white">Accessibility Service</p>
<p class="text-sm text-slate-600 dark:text-slate-400">Enables the app to block distracting apps and provide real-time usage insights.</p>
</div>
</div>
<div class="px-4 pb-4 flex justify-end">
<button class="bg-primary/10 text-primary text-sm font-bold py-2 px-4 rounded-md">Enable Service</button>
</div>
</div>
</div>
</div>
<div class="p-6">
<button class="w-full bg-primary text-white font-bold py-3 px-5 rounded-xl text-base">Continue</button>
</div>
</div>

</body></html>

<!-- Trial & Subscription Prompt -->
<!DOCTYPE html>
<html class="dark" lang="en"><head>
<meta charset="utf-8"/>
<meta charset="utf-8"/>
<title>Digital Wellness App - Subscription Management</title>
<link crossorigin="" href="https://fonts.gstatic.com/" rel="preconnect"/>
<link as="style" href="https://fonts.googleapis.com/css2?display=swap&amp;family=Inter:wght@400;500;700;900&amp;family=Noto+Sans:wght@400;500;700;900" onload="this.rel='stylesheet'" rel="stylesheet"/>
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined" rel="stylesheet"/>
<script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
<script>
        tailwind.config = {
            darkMode: "class",
            theme: {
                extend: {
                    colors: {
                        "primary": "#1173d4",
                        "background-light": "#f6f7f8",
                        "background-dark": "#101922",
                    },
                    fontFamily: {
                        "display": ["Inter", "Noto Sans", "sans-serif"]
                    },
                    borderRadius: {
                        "DEFAULT": "0.5rem",
                        "lg": "1rem",
                        "xl": "1.5rem",
                        "full": "9999px"
                    },
                },
            },
        }
    </script>
<style>
        body {
            min-height: max(884px, 100dvh);
        }
    </style>
<style>
    body {
      min-height: max(884px, 100dvh);
    }
  </style>
  </head>
<body class="bg-background-light dark:bg-background-dark font-display">
<div class="flex flex-col h-screen w-full">
<header class="flex items-center justify-between p-4 border-b border-slate-200 dark:border-slate-700">
<button class="p-2">
<span class="material-symbols-outlined text-slate-800 dark:text-white">arrow_back_ios_new</span>
</button>
<h1 class="text-xl font-bold text-slate-800 dark:text-white">Settings</h1>
<div class="w-10"></div>
</header>
<main class="flex-1 overflow-y-auto p-6">
<h2 class="text-2xl font-bold text-slate-800 dark:text-white mb-6">Subscription Management</h2>
<div class="bg-white dark:bg-slate-800 rounded-xl shadow-sm p-6 mb-6">
<div class="flex justify-between items-start mb-4">
<div>
<h3 class="text-lg font-bold text-slate-800 dark:text-white">Current Plan</h3>
<p class="text-base text-slate-600 dark:text-slate-300">Premium</p>
</div>
<span class="inline-flex items-center px-3 py-1 text-sm font-semibold text-green-700 bg-green-100 rounded-full dark:bg-green-900 dark:text-green-200">Active</span>
</div>
<div class="space-y-2">
<p class="text-sm text-slate-500 dark:text-slate-400">Your subscription renews on <span class="font-semibold text-slate-700 dark:text-slate-200">December 23, 2024</span>.</p>
<a class="text-sm font-semibold text-primary hover:underline" href="#">Manage billing</a>
</div>
</div>
<div class="bg-white dark:bg-slate-800 rounded-xl shadow-sm p-6 mb-8">
<h3 class="text-lg font-bold text-slate-800 dark:text-white mb-4">Premium Benefits</h3>
<ul class="space-y-3 text-slate-600 dark:text-slate-300">
<li class="flex items-center">
<span class="material-symbols-outlined text-green-500 mr-3">check_circle</span>
                        Unlimited app blocking
                    </li>
<li class="flex items-center">
<span class="material-symbols-outlined text-green-500 mr-3">check_circle</span>
                        Advanced scheduling
                    </li>
<li class="flex items-center">
<span class="material-symbols-outlined text-green-500 mr-3">check_circle</span>
                        Detailed usage statistics
                    </li>
<li class="flex items-center">
<span class="material-symbols-outlined text-green-500 mr-3">check_circle</span>
                        Priority support
                    </li>
</ul>
</div>
<div class="flex flex-col gap-3">
<button class="flex h-12 w-full items-center justify-center rounded-lg bg-primary px-5 text-base font-bold text-white">Renew Subscription</button>
<button class="flex h-12 w-full items-center justify-center rounded-lg bg-red-100 dark:bg-red-900/40 px-5 text-base font-bold text-red-600 dark:text-red-400">Unsubscribe</button>
</div>
</main>
</div>

</body></html>

<!-- Usage Statistics -->
<!DOCTYPE html>
<html class="dark" lang="en"><head>
<meta charset="utf-8"/>
<meta content="width=device-width, initial-scale=1.0" name="viewport"/>
<title>Stitch Design</title>
<link href="data:image/x-icon;base64," rel="icon" type="image/x-icon"/>
<link href="https://fonts.googleapis.com" rel="preconnect"/>
<link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect"/>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&amp;display=swap" rel="stylesheet"/>
<script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
<script id="tailwind-config">
        tailwind.config = {
            darkMode: "class",
            theme: {
                extend: {
                    colors: {
                        "primary": "#1173d4",
                        "background-light": "#f6f7f8",
                        "background-dark": "#101922",
                    },
                    fontFamily: {
                        "display": ["Inter"]
                    },
                    borderRadius: {
                        "DEFAULT": "0.5rem",
                        "lg": "1rem",
                        "xl": "1.5rem",
                        "full": "9999px"
                    },
                },
            },
        }
    </script>
<style>
        body {
            min-height: max(884px, 100dvh);
        }
    </style>
<style>
    body {
      min-height: max(884px, 100dvh);
    }
  </style>
<style>
    body {
      min-height: max(884px, 100dvh);
    }
  </style>
  </head>
<body class="bg-background-light dark:bg-background-dark font-display">
<div class="flex flex-col min-h-screen">
<header class="sticky top-0 z-10 bg-background-light/80 dark:bg-background-dark/80 backdrop-blur-sm">
<div class="flex items-center p-4">
<button class="p-2 text-slate-800 dark:text-white">
<svg fill="currentColor" height="24" viewBox="0 0 256 256" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="M224,128a8,8,0,0,1-8,8H59.31l58.35,58.34a8,8,0,0,1-11.32,11.32l-72-72a8,8,0,0,1,0-11.32l72-72a8,8,0,0,1,11.32,11.32L59.31,120H216A8,8,0,0,1,224,128Z"></path>
</svg>
</button>
<h1 class="text-lg font-bold text-slate-900 dark:text-white text-center flex-1 pr-10">Settings</h1>
</div>
</header>
<main class="flex-1 overflow-y-auto p-4 space-y-6">
<div class="space-y-6">
<div>
<h2 class="text-xs font-bold uppercase text-slate-500 dark:text-slate-400 px-4 mb-2">Account</h2>
<div class="bg-slate-100 dark:bg-slate-800/60 rounded-xl">
<div class="flex items-center justify-between p-4 border-b border-slate-200 dark:border-slate-700/60">
<div class="flex items-center gap-4">
<img alt="User avatar" class="h-12 w-12 rounded-full" src="https://lh3.googleusercontent.com/aida-public/AB6AXuD2UKPimaSZSP1N6ZBMyg1koPmS_Tma3MHC8i0srYutPnlpwAVT5uaADLSoXjnkCeUIeHir-Erc9SxXJk7NWFsy1DX27-rOCNTQHxZwn3j3uLXZDJd4zT2nusLRiFkV1QYV0hdJZu0_FjONQ6mgnSehbdRAyR9ylEVJz33WiJHkcZM_aB0hyhzxA91__kRlRKjRwlUTnzIQlO5pnotY2Pr-o-l8-6qm_g5Gye2uhV8nX_zVq9aNM1lcpECy-3x_jz_sNrhhfk8JR_c"/>
<div>
<p class="text-base font-semibold text-slate-800 dark:text-white">DigitalWell</p>
<p class="text-sm text-slate-500 dark:text-slate-400">digital.well@email.com</p>
</div>
</div>
<a class="text-sm font-medium text-primary hover:text-primary/80" href="#">Edit</a>
</div>
<a class="flex items-center justify-between p-4 text-red-500 dark:text-red-400" href="#">
<div class="flex items-center gap-4">
<svg class="text-red-500 dark:text-red-400" fill="none" height="24" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
<polyline points="16 17 21 12 16 7"></polyline>
<line x1="21" x2="9" y1="12" y2="12"></line>
</svg>
<span class="font-medium">Logout</span>
</div>
</a>
</div>
</div>
<div>
<h2 class="text-xs font-bold uppercase text-slate-500 dark:text-slate-400 px-4 mb-2">Subscription</h2>
<div class="bg-slate-100 dark:bg-slate-800/60 rounded-xl p-4">
<div class="flex items-center justify-between mb-2">
<div>
<p class="text-base font-semibold text-slate-800 dark:text-white">Premium Plan</p>
<p class="text-sm text-slate-500 dark:text-slate-400">Renews in 25 days</p>
</div>
<div class="bg-green-100 dark:bg-green-900/50 text-green-700 dark:text-green-300 text-xs font-bold px-2.5 py-1 rounded-full">
                                ACTIVE
                            </div>
</div>
<a class="w-full text-center bg-primary text-white font-semibold py-2.5 rounded-lg block mt-4 hover:bg-primary/90 transition-colors" href="#">Manage Subscription</a>
</div>
</div>
<div>
<h2 class="text-xs font-bold uppercase text-slate-500 dark:text-slate-400 px-4 mb-2">General</h2>
<div class="bg-slate-100 dark:bg-slate-800/60 rounded-xl">
<div class="flex items-center justify-between p-4 border-b border-slate-200 dark:border-slate-700/60">
<div class="flex items-center gap-4">
<svg class="text-slate-500 dark:text-slate-400" fill="none" height="24" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"></path>
</svg>
<span class="font-medium text-slate-800 dark:text-white">Dark Mode</span>
</div>
<label class="relative inline-flex items-center cursor-pointer">
<input checked="" class="sr-only peer" type="checkbox"/>
<div class="w-11 h-6 bg-slate-200 dark:bg-slate-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-slate-600 peer-checked:bg-primary"></div>
</label>
</div>
<a class="flex items-center justify-between p-4" href="#">
<div class="flex items-center gap-4">
<svg class="text-slate-500 dark:text-slate-400" fill="none" height="24" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="m12 14 4-4"></path>
<path d="M3.34 19a10 10 0 1 1 17.32 0"></path>
</svg>
<span class="font-medium text-slate-800 dark:text-white">Manage Blocked Apps</span>
</div>
<svg class="text-slate-400 dark:text-slate-500" fill="currentColor" height="20" viewBox="0 0 256 256" width="20" xmlns="http://www.w3.org/2000/svg">
<path d="M181.66,133.66l-80,80a8,8,0,0,1-11.32-11.32L164.69,128,90.34,53.66a8,8,0,0,1,11.32-11.32l80,80A8,8,0,0,1,181.66,133.66Z"></path>
</svg>
</a>
</div>
</div>
<div>
<h2 class="text-xs font-bold uppercase text-slate-500 dark:text-slate-400 px-4 mb-2">Notifications</h2>
<div class="bg-slate-100 dark:bg-slate-800/60 rounded-xl">
<div class="flex items-center justify-between p-4 border-b border-slate-200 dark:border-slate-700/60">
<div class="flex items-center gap-4">
<svg class="text-slate-500 dark:text-slate-400" fill="none" height="24" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"></path>
<path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"></path>
</svg>
<span class="font-medium text-slate-800 dark:text-white">Focus Session Alerts</span>
</div>
<label class="relative inline-flex items-center cursor-pointer">
<input class="sr-only peer" type="checkbox"/>
<div class="w-11 h-6 bg-slate-200 dark:bg-slate-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-slate-600 peer-checked:bg-primary"></div>
</label>
</div>
<div class="flex items-center justify-between p-4">
<div class="flex items-center gap-4">
<svg class="text-slate-500 dark:text-slate-400" fill="none" height="24" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="m17 2 4 4-4 4"></path>
<path d="M3 11v-1a4 4 0 0 1 4-4h14"></path>
<path d="m7 22-4-4 4-4"></path>
<path d="M21 13v1a4 4 0 0 1-4 4H3"></path>
</svg>
<span class="font-medium text-slate-800 dark:text-white">Usage Reminders</span>
</div>
<label class="relative inline-flex items-center cursor-pointer">
<input checked="" class="sr-only peer" type="checkbox"/>
<div class="w-11 h-6 bg-slate-200 dark:bg-slate-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-slate-600 peer-checked:bg-primary"></div>
</label>
</div>
</div>
</div>
<div>
<h2 class="text-xs font-bold uppercase text-slate-500 dark:text-slate-400 px-4 mb-2">Data &amp; Privacy</h2>
<div class="bg-slate-100 dark:bg-slate-800/60 rounded-xl">
<a class="flex items-center justify-between p-4 border-b border-slate-200 dark:border-slate-700/60" href="#">
<div class="flex items-center gap-4">
<svg class="text-slate-500 dark:text-slate-400" fill="none" height="24" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10"></path>
<path d="m9 12 2 2 4-4"></path>
</svg>
<span class="font-medium text-slate-800 dark:text-white">Privacy Policy</span>
</div>
<svg class="text-slate-400 dark:text-slate-500" fill="currentColor" height="20" viewBox="0 0 256 256" width="20" xmlns="http://www.w3.org/2000/svg">
<path d="M181.66,133.66l-80,80a8,8,0,0,1-11.32-11.32L164.69,128,90.34,53.66a8,8,0,0,1,11.32-11.32l80,80A8,8,0,0,1,181.66,133.66Z"></path>
</svg>
</a>
<a class="flex items-center justify-between p-4" href="#">
<div class="flex items-center gap-4">
<svg class="text-slate-500 dark:text-slate-400" fill="none" height="24" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="M12 21v-4"></path>
<path d="M16 17H8"></path>
<path d="M5 21a7 7 0 0 1 14 0M12 3v10"></path>
<path d="M10 5.5 12 3l2 2.5"></path>
<path d="m14 11-2 2-2-2"></path>
</svg>
<span class="font-medium text-slate-800 dark:text-white">Export My Data</span>
</div>
<svg class="text-slate-400 dark:text-slate-500" fill="currentColor" height="20" viewBox="0 0 256 256" width="20" xmlns="http://www.w3.org/2000/svg">
<path d="M181.66,133.66l-80,80a8,8,0,0,1-11.32-11.32L164.69,128,90.34,53.66a8,8,0,0,1,11.32-11.32l80,80A8,8,0,0,1,181.66,133.66Z"></path>
</svg>
</a>
</div>
</div>
<div>
<h2 class="text-xs font-bold uppercase text-slate-500 dark:text-slate-400 px-4 mb-2">Support</h2>
<div class="bg-slate-100 dark:bg-slate-800/60 rounded-xl">
<a class="flex items-center justify-between p-4 border-b border-slate-200 dark:border-slate-700/60" href="#">
<div class="flex items-center gap-4">
<svg class="text-slate-500 dark:text-slate-400" fill="none" height="24" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="M9.1 9a3 3 0 0 1 5.82 1c0 2-3 3-3 3"></path>
<path d="M12 17h.01"></path>
<circle cx="12" cy="12" r="10"></circle>
</svg>
<span class="font-medium text-slate-800 dark:text-white">Help Center</span>
</div>
<svg class="text-slate-400 dark:text-slate-500" fill="currentColor" height="20" viewBox="0 0 256 256" width="20" xmlns="http://www.w3.org/2000/svg">
<path d="M181.66,133.66l-80,80a8,8,0,0,1-11.32-11.32L164.69,128,90.34,53.66a8,8,0,0,1,11.32-11.32l80,80A8,8,0,0,1,181.66,133.66Z"></path>
</svg>
</a>
<a class="flex items-center justify-between p-4" href="#">
<div class="flex items-center gap-4">
<svg class="text-slate-500 dark:text-slate-400" fill="none" height="24" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
<circle cx="12" cy="12" r="10"></circle>
<path d="M12 16v-4"></path>
<path d="M12 8h.01"></path>
</svg>
<span class="font-medium text-slate-800 dark:text-white">About</span>
</div>
<svg class="text-slate-400 dark:text-slate-500" fill="currentColor" height="20" viewBox="0 0 256 256" width="20" xmlns="http://www.w3.org/2000/svg">
<path d="M181.66,133.66l-80,80a8,8,0,0,1-11.32-11.32L164.69,128,90.34,53.66a8,8,0,0,1,11.32-11.32l80,80A8,8,0,0,1,181.66,133.66Z"></path>
</svg>
</a>
</div>
</div>
</div>
</main>
<footer class="sticky bottom-0 bg-background-light/80 dark:bg-background-dark/80 backdrop-blur-sm border-t border-slate-200 dark:border-slate-800">
<nav class="flex justify-around items-center h-16">
<a class="flex flex-col items-center gap-1 text-slate-500 dark:text-slate-400" href="#">
<svg fill="currentColor" height="24" viewBox="0 0 256 256" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="M221.87,83.16A104.1,104.1,0,1,1,195.67,49l22.67-22.68a8,8,0,0,1,11.32,11.32l-96,96a8,8,0,0,1-11.32-11.32l27.72-27.72a40,40,0,1,0,17.87,31.09,8,8,0,1,1,16-.9,56,56,0,1,1-22.38-41.65L184.3,60.39a87.88,87.88,0,1,0,23.13,29.67,8,8,0,0,1,14.44-6.9Z"></path>
</svg>
<span class="text-xs font-medium">Focus</span>
</a>
<a class="flex flex-col items-center gap-1 text-slate-500 dark:text-slate-400" href="#">
<svg fill="currentColor" height="24" viewBox="0 0 256 256" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="M216,40H40A16,16,0,0,0,24,56V200a16,16,0,0,0,16,16H216a16,16,0,0,0,16-16V56A16,16,0,0,0,216,40ZM200,176a8,8,0,0,1,0,16H56a8,8,0,0,1-8-8V72a8,8,0,0,1,16,0v62.92l34.88-29.07a8,8,0,0,1,9.56-.51l43,28.69,43.41-36.18a8,8,0,0,1,10.24,12.3l-48,40a8,8,0,0,1-9.56.51l-43-28.69L64,155.75V176Z"></path>
</svg>
<span class="text-xs font-medium">Statistics</span>
</a>
<a class="flex flex-col items-center gap-1 text-primary" href="#">
<svg fill="currentColor" height="24" viewBox="0 0 256 256" width="24" xmlns="http://www.w3.org/2000/svg">
<path d="M128,80a48,48,0,1,0,48,48A48.05,48.05,0,0,0,128,80Zm0,80a32,32,0,1,1,32-32A32,32,0,0,1,128,160Zm88-29.84q.06-2.16,0-4.32l14.92-18.64a8,8,0,0,0,1.48-7.06,107.21,107.21,0,0,0-10.88-26.25,8,8,0,0,0-6-3.93l-23.72-2.64q-1.48-1.56-3-3L186,40.54a8,8,0,0,0-3.94-6,107.71,107.71,0,0,0-26.25-10.87,8,8,0,0,0-7.06,1.49L130.16,40Q128,40,125.84,40L107.2,25.11a8,8,0,0,0-7.06-1.48A107.6,107.6,0,0,0,73.89,34.51a8,8,0,0,0-3.93,6L67.32,64.27q-1.56,1.49-3,3L40.54,70a8,8,0,0,0-6,3.94,107.71,107.71,0,0,0-10.87,26.25,8,8,0,0,0,1.49,7.06L40,125.84Q40,128,40,130.16L25.11,148.8a8,8,0,0,0-1.48,7.06,107.21,107.21,0,0,0,10.88,26.25,8,8,0,0,0,6,3.93l23.72,2.64q1.49,1.56,3,3L70,215.46a8,8,0,0,0,3.94,6,107.71,107.71,0,0,0,26.25,10.87,8,8,0,0,0,7.06-1.49L125.84,216q2.16.06,4.32,0l18.64,14.92a8,8,0,0,0,7.06,1.48,107.21,107.21,0,0,0,26.25-10.88,8,8,0,0,0,3.93-6l2.64-23.72q1.56-1.48,3-3L215.46,186a8,8,0,0,0,6-3.94,107.71,107.71,0,0,0,10.87-26.25,8,8,0,0,0-1.49-7.06Zm-16.1-6.5a73.93,73.93,0,0,1,0,8.68,8,8,0,0,0,1.74,5.48l14.19,17.73a91.57,91.57,0,0,1-6.23,15L187,173.11a8,8,0,0,0-5.1,2.64,74.11,74.11,0,0,1-6.14,6.14,8,8,0,0,0-2.64,5.1l-2.51,22.58a91.32,91.32,0,0,1-15,6.23l-17.74-14.19a8,8,0,0,0-5-1.75h-.48a73.93,73.93,0,0,1-8.68,0,8,8,0,0,0-5.48,1.74L100.45,215.8a91.57,91.57,0,0,1-15-6.23L82.89,187a8,8,0,0,0-2.64-5.1,74.11,74.11,0,0,1-6.14-6.14,8,8,0,0,0-5.1-2.64L46.43,170.6a91.32,91.32,0,0,1-6.23-15l14.19-17.74a8,8,0,0,0,1.74-5.48,73.93,73.93,0,0,1,0-8.68,8,8,0,0,0-1.74-5.48L40.2,100.45a91.57,91.57,0,0,1,6.23-15L69,82.89a8,8,0,0,0,5.1-2.64,74.11,74.11,0,0,1,6.14-6.14A8,8,0,0,0,82.89,69L85.4,46.43a91.32,91.32,0,0,1,15-6.23l17.74,14.19a8,8,0,0,0,5.48,1.74,73.93,73.93,0,0,1,8.68,0,8,8,0,0,0,5.48-1.74L155.55,40.2a91.57,91.57,0,0,1,15,6.23L173.11,69a8,8,0,0,0,2.64,5.1,74.11,74.11,0,0,1,6.14,6.14,8,8,0,0,0,5.1,2.64l22.58,2.51a91.32,91.32,0,0,1,6.23,15l-14.19,17.74A8,8,0,0,0,199.87,123.66Z"></path>
</svg>
<span class="text-xs font-medium">Settings</span>
</a>
</nav>
<div class="h-safe-bottom"></div>
</footer>
</div>

</body></html>