
.. digraph:: example

   node [fontname = "Liberation Sans", fontsize = 12];
   edge [fontname = "Liberation Sans", fontsize = 11];

   bob [shape = ellipse, style = filled, color = palegreen, label = "<bob>"];
   alice [shape = ellipse, style = filled, color = palegreen, label = "<alice>"];
   carol [shape = ellipse, style = filled, color = palegreen, label = "<carol>"];

   bob_name [shape = box, style = filled, color = peachpuff, label = "\"Bob\"" ];
   alice_name [shape = box, style = filled, color = peachpuff, label = "\"Alice\"" ];
   carol_name [shape = box, style = filled, color = peachpuff, label = "\"Carol\"" ];

   bob -> bob_name [label = "foaf:name"];
   alice -> alice_name [label = "foaf:name"];
   carol -> carol_name [label = "foaf:name"];

   bob -> alice [label = "foaf:knows"] ;
   bob -> carol [label = "foaf:knows"] ;
