# IntelliJ Platform Plugin Development — Reference

Target: Kotlin, IntelliJ Platform **2025.1+**, **Gradle IntelliJ Platform plugin 2.x**.
Source of truth for current API: IntelliJ Platform SDK (`https://plugins.jetbrains.com/docs/intellij/`).
When a fact about the *running* IDE matters (which EPs/plugins/services exist), prefer the
IDE Introspector tools (see `IDE-INTROSPECTOR.md`) over memory.

---

## 1. Plugin anatomy

```
src/main/
├── kotlin/<pkg>/        # plugin code (one public class per file)
├── resources/META-INF/
│   └── plugin.xml       # manifest: id, name, depends, extensions, actions, listeners
├── resources/messages/  # i18n .properties (resource bundle)
├── grammars/            # *.flex (lexer) + *.bnf (parser/PSI) for custom languages
└── gen/                 # GENERATED parser/lexer/PSI — never edit by hand
```

`plugin.xml` essentials:

```xml
<idea-plugin>
  <id>com.example.myplugin</id>                 <!-- immutable, globally unique -->
  <name>My Plugin</name>
  <vendor>me</vendor>
  <depends>com.intellij.modules.platform</depends>
  <depends optional="true" config-file="my-yaml.xml">org.jetbrains.plugins.yaml</depends>
  <resource-bundle>messages.MyBundle</resource-bundle>

  <extensions defaultExtensionNs="com.intellij">
    <!-- contribute behavior here -->
  </extensions>
  <actions>
    <!-- menu / toolbar actions here -->
  </actions>
</idea-plugin>
```

- **`<depends>`**: declare every platform module / plugin you use. Optional dependencies load a
  secondary `config-file` only when the dependency is present (the idiomatic way to add e.g. YAML/JSON
  integration without a hard requirement).
- `sinceBuild` (e.g. `251` for 2025.1) is set in `gradle.properties` / the Gradle plugin config, not by hand in the manifest.

---

## 2. Extension points (EPs) — the contribution mechanism

Almost all features are contributed by registering an **extension** against an **extension point**.
Two EP kinds:
- **Interface EP** — you provide an `implementationClass`; the platform instantiates your class.
- **Bean-class EP** — you provide attributes that the platform reads into a bean (e.g. `language`, `id`).

```xml
<extensions defaultExtensionNs="com.intellij">
  <lang.parserDefinition language="MyLang"
      implementationClass="com.example.MyParserDefinition"/>
  <annotator language="MyLang"
      implementationClass="com.example.MyAnnotator"/>
  <completion.contributor language="MyLang"
      implementationClass="com.example.MyCompletionContributor"/>
  <fileBasedIndex implementation="com.example.MyIndex"/>
</extensions>
```

To **define your own EP** for others (or your own sub-plugins) to extend:

```xml
<extensionPoints>
  <extensionPoint name="myFeature" interface="com.example.MyFeature" dynamic="true"/>
</extensionPoints>
```

Consume it: `MyFeatureEP.EP_NAME.extensionList` (declare `EP_NAME = ExtensionPointName.create("com.example.myFeature")`).
Mark EPs `dynamic="true"` so the plugin can be installed/updated without IDE restart.

**Discovering EPs in the live IDE** (more reliable than docs): `arch.list_extension_points
nameContains="completion"`, then `arch.list_extensions_for_ep` / `arch.find_extenders_of`.

---

## 3. Services (prefer light `@Service`)

Services are the platform's DI singletons. Three levels: **application**, **project**, **module**.

```kotlin
@Service(Service.Level.PROJECT)
class MyProjectService(private val project: Project) {
    fun doThing() { /* ... */ }
}

// retrieval — never construct a service yourself:
val svc = project.service<MyProjectService>()      // project level
val app = service<MyAppService>()                   // application level
```

- **Light services** (`@Service` annotation, no XML) are preferred: less boilerplate, lazy, type-safe.
  Use the XML forms (`<applicationService>` / `<projectService>` with `serviceInterface` +
  `serviceImplementation`) only when you need an **interface/impl split**, a **test/headless override**,
  or `preload`.
- **Constructor injection**: a project service may take `Project` (and `CoroutineScope` on recent
  platforms) as constructor params. Avoid heavy work in the constructor — services are created lazily
  but should still init fast.
- **Persistence**: implement `PersistentStateComponent<T>` and annotate with `@State(...)` to round-trip
  settings to XML. Keep the state class a simple POJO/data holder.

Inspect live services: `services.list area="project" providedByPlugin="<id>"`,
`services.find target="<FQCN>"`.

---

## 4. Listeners & the message bus

Two ways to react to events:

1. **Declarative listeners** in `plugin.xml` (preferred for static subscriptions):

```xml
<applicationListeners>
  <listener class="com.example.MyVfsListener"
            topic="com.intellij.openapi.vfs.newvfs.BulkFileListener"/>
</applicationListeners>
<projectListeners>
  <listener class="com.example.MyEditorListener"
            topic="com.intellij.openapi.fileEditor.FileEditorManagerListener"/>
</projectListeners>
```

2. **Runtime subscription** via the message bus when you need dynamic (un)subscription:

```kotlin
val conn = project.messageBus.connect(parentDisposable)
conn.subscribe(FileEditorManagerListener.TOPIC, object : FileEditorManagerListener { /* ... */ })
```

Define your own channel with a `Topic`:

```kotlin
interface MyListener { fun onChanged() ; companion object {
    @JvmField val TOPIC = Topic.create("my changes", MyListener::class.java) } }
// publish:
project.messageBus.syncPublisher(MyListener.TOPIC).onChanged()
```

Inspect live: `events.list_topics providedByPlugin="<id>"`, `events.list_listeners
topicContains="FileEditor"`, `events.find_listeners_of_topic topicClass="<FQCN>"`.

---

## 5. Actions

```kotlin
class MyAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        // ...
    }
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
    override fun getActionUpdateThread() = ActionUpdateThread.BGT  // or EDT if you touch Swing
}
```

```xml
<actions>
  <action id="com.example.MyAction" class="com.example.MyAction" text="Do Thing"
          description="Does the thing">
    <add-to-group group-id="ToolsMenu" anchor="last"/>
    <keyboard-shortcut keymap="$default" first-keystroke="control alt D"/>
  </action>
</actions>
```

`getActionUpdateThread()` is **required** on modern platforms — return `BGT` unless `update()` reads
Swing state. Keep `update()` cheap (it runs often).

---

## 6. Threading model (critical)

- **Read action** — any read of PSI / VFS / indexes / project model. From a background thread wrap in
  `ReadAction.compute { ... }` or a coroutine **read action** (`readAction { ... }`). The EDT holds an
  implicit read lock.
- **Write action** — mutating PSI/document/project model. Must run on **EDT inside a write action**;
  for document/PSI edits use `WriteCommandAction.runWriteCommandAction(project) { ... }` (undo-aware).
- **EDT** — all Swing/UI access. Never block it. Bounce to EDT with
  `ApplicationManager.getApplication().invokeLater { }` or `withContext(Dispatchers.EDT)`.
- **Background** — slow work (network, parsing, large scans) belongs on a background coroutine /
  `Dispatchers.Default`/`IO`. Use the service's injected `CoroutineScope`; don't spawn raw threads.
- **Dumb mode** — during indexing, index-backed APIs throw `IndexNotReadyException`. Guard with
  `DumbService.getInstance(project).runWhenSmart { }` or implement `DumbAware` only when the feature
  genuinely works without indexes. Check live state with the `ide.indexing_status` tool.
- **Cancellation** — honor `ProgressManager.checkCanceled()` / coroutine cancellation in long loops.

Modern code prefers **Kotlin coroutines** (structured concurrency via the service `CoroutineScope`,
`Dispatchers.EDT`/`Default`, `readAction`/`writeAction`, `withBackgroundProgress`) over raw
`invokeLater` + `Task.Backgroundable`, though both are valid.

---

## 7. PSI, references, and language features

- **PSI** is the syntactic/semantic tree. Custom languages get a `ParserDefinition`, `PsiElement`
  hierarchy, and (optionally) `PsiNamedElement` for renameable symbols.
- **References** (`PsiReference` / `PsiReferenceContributor`) implement Ctrl-click navigation and
  rename. Implement `resolve()` and `getVariants()` (for completion).
- **Annotators** (`Annotator`) add highlighting/inspection-like decorations during analysis; keep them
  fast and side-effect-free.
- **Inspections** (`LocalInspectionTool`) provide problems + quick fixes; **intentions**
  (`IntentionAction`) provide context actions.
- **Completion** (`CompletionContributor`) supplies suggestions; **folding** (`FoldingBuilder`),
  **brace matching** (`PairedBraceMatcher`), **commenter** (`Commenter`) round out language UX.
- **Find usages** uses `UsageSearcher` / `CustomUsageSearcher`; structure view via `StructureViewModel`.

Inspect a live file's PSI: `psi.get_structure`, `psi.get_references`, `psi.find_usages`
(see IDE-INTROSPECTOR). Resolve any class's API: `code.find_class` → `code.list_members` /
`code.get_source`.

---

## 8. Custom language with Grammar-Kit

Pipeline: `*.flex` (JFlex lexer) + `*.bnf` (Grammar-Kit parser/PSI grammar) → generated lexer,
parser, and PSI under `src/main/gen/`.

Gradle wiring (Grammar-Kit plugin):

```kotlin
tasks {
    generateLexer {
        sourceFile.set(file("src/main/grammars/MyLang.flex"))
        targetOutputDir.set(file("src/main/gen/com/example/lang/lexer"))
    }
    generateParser {
        sourceFile.set(file("src/main/grammars/MyLang.bnf"))
        targetRootOutputDir.set(file("src/main/gen"))
        pathToParser.set("com/example/lang/parser/MyLangParser.java")
        pathToPsiRoot.set("com/example/lang/psi")
    }
    compileKotlin { dependsOn("generateLexer", "generateParser") }
    compileJava   { dependsOn("generateLexer", "generateParser") }
}
sourceSets { main { java.srcDirs("src/main/gen") } }
```

Rules: edit only the `.flex`/`.bnf`; regenerate after changes; register a `ParserDefinition`,
`LanguageFileType`, and `SyntaxHighlighterFactory` in `plugin.xml`. Hand-written PSI helpers go in a
`psi/` package (kept separate from generated `gen/.../psi`).

---

## 9. LSP integration via LSP4IJ

When a language already has an external language server, **LSP4IJ** (Red Hat) bridges it instead of
hand-writing PSI features.

- Register a `LanguageServerFactory` (server support provider) — for a manually-started server use a
  socket-based `StreamConnectionProvider` (e.g. connect to `localhost:<port>`).
- Implement a `DocumentMatcher` to limit activation to relevant projects/files only (don't attach the
  server to every `.yaml` in every project).
- Prefer the standard LSP features LSP4IJ provides (completion, diagnostics, folding, signature help,
  structure view) over re-implementing them natively.
- **Avoid a direct compile dependency on LSP4J** from your code — go through LSP4IJ abstractions to
  prevent `ClassCastException` from classloader mismatches.

---

## 10. Gradle build (IntelliJ Platform plugin 2.x)

```kotlin
plugins {
    id("java")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intelliJPlatform)
    alias(libs.plugins.grammarkit)   // if you have a custom language
}
kotlin { jvmToolchain(21) }          // 2025.1 targets JDK 21
repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}
dependencies {
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))
        bundledPlugins(/* e.g. "org.jetbrains.plugins.yaml" via property */)
        plugins(/* marketplace deps via property */)
        testFramework(TestFrameworkType.Platform)
    }
}
```

Key tasks: `runIde` (sandbox IDE), `buildPlugin` (zip → `build/distributions/`), `verifyPlugin`
(compatibility), `signPlugin` + `publishPlugin` (Marketplace). Declare bundled/marketplace plugin
deps via `gradle.properties` properties, not literals, to keep versions in one place.

---

## 11. Testing

- Extend `BasePlatformTestCase` (a.k.a. light fixture tests) for most PSI/inspection/completion tests;
  put inputs/golden files under `src/test/testData/`.
- Use `myFixture.configureByText/File`, `checkResult`, `testHighlighting`, `completeBasic`, etc.
- For heavier scenarios use `HeavyPlatformTestCase` / `CodeInsightTestFixture`.
- Track coverage with Kover; gate on `verifyPlugin` in CI.

---

## 12. Common pitfalls

- ❌ Editing generated `src/main/gen/` — regenerate from grammar instead.
- ❌ `new`-ing a service or holding a stale `Project`/`PsiElement` across indexing → use services +
  re-resolve; never cache PSI long-term.
- ❌ Touching PSI/VFS off a read action, or mutating without a write action → threading assertions fail.
- ❌ Blocking the EDT with I/O or a big scan → UI freeze; move to background coroutine.
- ❌ Missing `getActionUpdateThread()` or doing heavy work in `AnAction.update()`.
- ❌ Hard `<depends>` on an optional plugin → plugin fails to load where that plugin is absent; use
  `optional="true"` + `config-file`.
- ❌ Activating language/LSP features globally instead of gating on a project/document matcher.
- ❌ Hardcoded user strings / `println` → use the resource bundle and platform `Logger`.
