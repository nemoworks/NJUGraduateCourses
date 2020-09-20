function [ale,art,fc] = configurable(alg,n,x_size,y_size,anchor_num,range)
%UNTITLE 此处显示有关此函数的摘要
%   此处显示详细说明
if alg=="lap"
    fc=0;
    batch=100; % number of batches
    iterations=5; % number of iterations


    ale=zeros(batch,1);
    rt=zeros(batch,1);

    %laplacian process
    i = 0;
    while(i<batch) 
        % generate random (x,y) locations for sensor nodes
        true_loc=generate_random_network(n,x_size,y_size);

        % generate edgelist and node adjacency matrix
        % the first two columns in edge list is the tail and head vertex id
        % the thrid column is the distance of the edge
        % adjacency matrix is a n*n sparse matrix
        [edgelist,~]=getedges(true_loc,range);

        %get anchor nodes, there are anchor_num anchor nodes on each side of the
        %rectangular, but actual number of anchors may change.
        %the first column in anchor is the node id of the anchor, the next two
        %columns are x and y coordinates
        anchor=getanchor(true_loc,anchor_num);

        edgeweight=ones(size(edgelist,1),1); % initialize edge weights to 1
        tic
        for loops= 1:iterations %iteratively sove the balanced srping network problem
            %You should write your own program to balance the spring network using
            %graph laplacian
            est_loc= balancenet(n,[edgelist(:,1:2) edgeweight],anchor);
            if(isnan(est_loc))
                fc=fc+1;
                break
            end
            %You should write your own program to adjust the weight (stiffness) of
            %edges
            edgeweight=adjustweight(est_loc,[edgelist(:,1:2) edgeweight],range);
        end
        if(isnan(est_loc))
            continue
        end
        running_time = toc;
        errors = (true_loc-est_loc).';
        average_loc_error = sum(sqrt(sum(errors.*errors)))/(n-size(anchor,1));
        if isnan(average_loc_error)
            continue;
        end
        i=i+1;
        rt(i)=running_time;
        ale(i)=average_loc_error;
    end

    art = sum(rt)/batch;

else
    batch=100; % number of batches
    fc=0;

    ale=zeros(batch,1);
    rt=zeros(batch,1);

    %laplacian process
    i = 0;
    while(i<batch)
        % generate random (x,y) locations for sensor nodes
        true_loc=generate_random_network(n,x_size,y_size);

        % generate edgelist and node adjacency matrix
        % the first two columns in edge list is the tail and head vertex id
        % the thrid column is the distance of the edge
        % adjacency matrix is a n*n sparse matrix
        [edgelist,~]=getedges(true_loc,range);

        %get anchor nodes, there are anchor_num anchor nodes on each side of the
        %rectangular, but actual number of anchors may change.
        %the first column in anchor is the node id of the anchor, the next two
        %columns are x and y coordinates
        anchor=getanchor(true_loc,anchor_num);


        %use the edge list to get the relative position of sensors
        %you need to write your own code for mds function.
        tic
        mds_loc=mds(n,edgelist(:,1:2));

        if(isnan(mds_loc))
            fc=fc+1;
            continue;
        end

        % Do the scaling and rotation through linear regression
        % put the estimated anchor location as the training data
        training_data=[mds_loc(anchor(:,1),:) ones(size(anchor,1),1)];
        % the output must be the ture location of the anchors
        true_val=anchor(:,2:3);

        theta=gradientdescent(training_data,true_val,0.01,1000);
        est_loc=[mds_loc ones(n,1)]*theta;
        running_time = toc;
        errors = (true_loc-est_loc).';
        average_loc_error = sum(sqrt(sum(errors.*errors)))/(n-size(anchor,1));
        i=i+1;
        rt(i)=running_time;
        ale(i)=average_loc_error;
    end

    art = sum(rt)/batch;

end
end

