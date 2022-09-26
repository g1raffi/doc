---
weight: 32
title: Sed

---

## SED

Inline replace user password files with sed:

`sed -rEi "s/.*(user.*):(.*)@.*/\1,\2/g" users.csv`
