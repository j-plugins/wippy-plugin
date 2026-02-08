# wippy-plugin

![Build](https://github.com/xepozz/wippy-plugin/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/30113-wippy.svg)](https://plugins.jetbrains.com/plugin/30113-wippy)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/30113-wippy.svg)](https://plugins.jetbrains.com/plugin/30113-wippy)

<!-- Plugin description -->

[Github](https://github.com/j-plugins/wippy-plugin) | [Telegram](https://t.me/jb_plugins/755) | [Documentation](https://home.wj.wippy.ai) | [Donation](https://github.com/xepozz/xepozz?tab=readme-ov-file#become-a-sponsor)

## Wippy

IntelliJ IDEA plugin that provides first-class support for the Wippy agentic platform. 
It understands registry entry kinds (function.lua, agent.gen1, http.endpoint, etc.), validates YAML configurations, and offers smart navigation between entry definitions and their Lua implementations.

The plugin also integrates with Wippy's documentation API to provide inline help and autocompletion for Wippy-specific Lua modules like llm, process, registry, and others — making it easier for both humans and AI agents to develop within the Wippy ecosystem.

## Support the Project

If you find this plugin useful, you can support its development:

[<img height="28" src="https://github.githubassets.com/assets/patreon-96b15b9db4b9.svg"> Patreon](https://patreon.com/xepozz)
|
[<img height="28" src="https://github.githubassets.com/assets/buy_me_a_coffee-63ed78263f6e.svg"> Buy me a coffee](https://buymeacoffee.com/xepozz)
|
[<img height="28" src="https://boosty.to/favicon.ico"> Boosty](https://boosty.to/xepozz)

<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "wippy-plugin"</kbd> >
  <kbd>Install</kbd>

- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/30113-wippy) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/30113-wippy/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/xepozz/wippy-plugin/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
