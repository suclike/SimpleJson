* **No slow reflection, full performance**: SimpleJson uses compile time annotation processing to generate performant and efficient implementations of models and parsers. No performance hit due to reflection at runtime.
* **Most errors caught at compile time**: SimpleJson will check for most common errors at compile time and give you useful and detailed error messages.
* **Generates real debuggable code**: You can view the model and parser implementations at any time and debug any error and behaviour. No more guessing what went wrong.
* **Easy to use and quick to setup**: Getting SimpleJson to work requires no setup and after adding only a few annotations on your models you are good to go.
* **Works with ProGuard**: With SimpleJson you don't need to mess around with ProGuard rules and worry about keeping the right classes. Every bit of your code can be obfuscated without worrying about a thing.
* **Works with Retrofit2**: SimpleJson automatically detects if Retrofit2 is used in your project and automatically sets everything up so that Retrofit2 can work with your entities seamlessly!

# How to add it to your project

If you are using the new Jack compiler all you have to add is these two dependencies:

```groovy
compile 'com.github.wrdlbrnft:simple-json:0.1.0.10'
annotationProcessor 'com.github.wrdlbrnft:simple-json-processor:0.1.0.10'
```

If you are not using Jack you can use the android-apt Gradle plugin instead of using the `annotationProcessor` configuration 
to add the annotation processor of SimpleJson to your module. 

# Basic Usage

SimpleJson works exclusively with interfaces. It generates an implementation of those interface for you as well as a parser which translates it into json. 
To get started just annotate your interface with `@JsonEntity` and use `@FieldName` to tell SimpleJson how to map elements in the json to the getters.

```java
@JsonEntity
public interface ExampleModel {
    
  @FieldName("id")
  long getId();

  @FieldName("text")
  String getText();
}
```

A JSON that corrosponds to the above interface would look something like this:

```json
{
  "text": "some example text",
  "id": 27
}
```

For each interface a factory class is generated which can be used to turn json into your entities or the other way around. 
These factory classes are dynamically generated and are usually names by adding an s to the end of the interface name. If the interface name already ends with an s then the word Factory is appended.

```java
ExampleModel model = ExampleModels.fromJson(json);
```

If you have an array of json objects like this:

```json
[
  {
    "text": "qwerty",
    "id": 27
  },
  {
    "text": "asdfdsa",
    "id": 37
  },
  {
    "text": "hello world",
    "id": 47
  }
]
```

Then you can parse that json by calling `fromJsonArray()`:

```java
List<ExampleModel> models = ExampleModels.fromJsonArray(json);
```

Translating an entity into json from an entity or a `Collection` of entities works by calling `toJson()`:

```java
String json = ExampleModels.toJson(model);
...
List<ExampleModel> models = ...;
String jsonArray = SimpleJson.toJson(ExampleModel.class, models);
```

Each factory class also has a `create()` method which can be used to create new instances of your entities without you having to implement them:

```java
ExampleModel model = ExampleModels.create(27L, "text");
```

# Optional fields

If there is an optional element in a JSON you want to parse just annotate the corrosponding getter with `@Optional`. If the element is missing from the json then it will be parsed as `null`. If an element is not annotated with `@Optional` and it is missing from the JSON than a `SimpleJsonException` will be thrown! 

```java
@JsonEntity
public interface ExampleModel {
    
  @FieldName("id")
  long getId();

  @FieldName("text")
  String getText();

  @Optional
  @FieldName("value")
  String getOptionalValue();
}
```

**Note:** Methods annotated with `@Optional` cannot return primitive values! Use boxed values instead.

# Mapping Enums

SimpleJson can map Enums from and to JSON for you! To use an enum in SimpleJson just add the `@JsonEnum` annotation. You can then define the mappings of each value with the `@MapTo` annotation.
 You can also use `@MapDefault` to define default mapping values if no other mapping applies. If no default value is defined then a `SimpleJsonException` will be thrown.

```java
@JsonEnum
public enum ExampleEnum {

  @MapTo("a")
  VALUE_A,
    
  @MapTo("b")
  VALUE_B,
    
  @MapTo("c")
  VALUE_C
}
```

By annotating the enum like above `VALUE_A` will be mapped to the String `"a"` in the JSON, `VALUE_B` will be mapped to `"b"` and so on. If you parse a JSON and the String `"c"` is encountered in an element which should be parsed as `ExampleEnum` then it will be mapped to `VALUE_C`, if `"b"` is encountered it will be mapped to `VALUE_B` and so on.

# Collections and Child Entities

You can also work with complex models and child entities! Consider some like this:

```java
@JsonEntity
public interface Parent {

  @FieldName("types")
  List<Type> getTypes();
    
  @FieldName("children")
  Set<Child> getChildren();
}

@JsonEntity
public interface Child {

  @FieldName("text")
  String getText();
  
  @FieldName("enabled")
  boolean isEnabled();
  
  @FieldName("value")
  double getValue();
}

@JsonEnum
public enum Type {
  @MapTo("a") A,
  @MapTo("b") B,
  @MapTo("c") C
  @MapDefault D
}
```

Collections like `List` or `Set` are represented as array in JSON. Child entities will be parsed recursively and if a `Parent` entity is translated to JSON the result would look something like this:

```json
{
  "types": ["a", "b"],
  "children": [
    {
      "text": "some text",
      "enabled": 1,
      "value": 23.7
    },
    {
      "text": "some other text",
      "enabled": 0,
      "value": 0.0
    },
    {
      "text": "example",
      "enabled": 1,
      "value": 1234.5
    }
  ]
}
```

# Retrofit2

If you are using Retrofit2 to make your API calls then SimpleJson will automatically generate a 
`SimpleJsonConverterFactory` for you which enables Retrofit2 to seamlessly work with your SimpleJson entities.
You can add the `SimpleJsonConverterFactory` when you create your `Retrofit` instance like this:

```java
Retrofit retrofit = new Retrofit.Builder()
    .baseUrl("https://your.backend.com")
    .addConverterFactory(new SimpleJsonConverterFactory())
    .build();
```
