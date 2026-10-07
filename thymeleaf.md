**Variables: `th:with`**
```html
<div th:with="total=${set.reps * set.weight}, name=${exercise.name}">
  <span th:text="${name + ': ' + total}"></span>
</div>
```

**Grouping without extra HTML: `th:block`**
```html
<th:block th:with="sets=${session.sets}" th:if="${!sets.isEmpty()}">
  <h3>Sets</h3>
  <p th:each="s : ${sets}" th:text="${s.reps}"></p>
</th:block>
```
`th:block` disappears from the output, so it works as an invisible wrapper.

**Conditions**
```html
<p th:if="${x > 0}">…</p>   <p th:unless="${x > 0}">…</p>
<div th:switch="${day.type}">
  <p th:case="'REST'">Rest</p>
  <p th:case="*">Training</p>
</div>
<span th:text="${active ? 'Yes' : 'No'}"></span>
<span th:text="${endDate ?: 'No end'}"></span>   <!-- elvis: shows 'No end' when endDate is null -->
```

**Loops with status**
```html
<tr th:each="ex, stat : ${exercises}" th:classappend="${stat.odd} ? 'odd'">
  <td th:text="${stat.count}"></td>   <!-- also: index, first, last, size -->
</tr>
```

**Filtering and picking fields from lists**
```html
${sets.?[weight > 50]}        <!-- filter: keep sets heavier than 50 -->
${sets.![weight]}             <!-- map: list of just the weights -->
${#aggregates.sum(sets.![weight])}
${#lists.size(sets)}  ${#strings.isEmpty(s)}  ${#temporals.format(date,'yyyy-MM-dd')}
```

**Reusable chunks: fragments**
```html
<div th:fragment="setRow(s)"> <span th:text="${s.reps}"></span> </div>
<div th:replace="~{workout-session :: setRow(${set})}"></div>
```

**Grouping (e.g. sets per exercise):** build a `Map<Exercise, List<Set>>` in the controller (use a `LinkedHashMap` to keep the order), then loop over it:
```html
<th:block th:each="entry : ${setsByExercise}">
  <h3 th:text="${entry.key.name}"></h3>
  <p th:each="s : ${entry.value}" th:text="${s.reps}"></p>
</th:block>
```
There's no built-in group-by in Thymeleaf. Building the map in the controller keeps the template simple.
