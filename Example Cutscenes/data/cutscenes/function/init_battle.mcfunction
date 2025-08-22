tellraw @s {"text": "Pretend something cool started, like a battle or something", "italic": true, "color": "gold"}
give @s diamond[custom_name="{\"text\": \"A very fine Diamond\"}", rarity="epic", max_stack_size=99]
execute if cutscene start_reason @s contains kromer run tellraw @s {"text": "You found an easter egg! But did you add the brackets?", "color": "yellow"}
execute if cutscene start_reason @s equals "[[kromer]]" run give @s emerald[custom_name="KROMER"] 1997
execute if cutscene end_reason @s command run tellraw @s {"text": "...but it was aborted.", "color": "gray"}